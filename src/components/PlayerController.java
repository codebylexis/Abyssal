package components;

import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import core.GameObject;
import core.KeyListener;
import core.Window;
import physics2d.RaycastInfo;
import physics2d.components.Rigidbody2D;

/**
 * Handles player movement and jumping. Velocity is integrated manually rather
 * than relying purely on JBox2D so we get tight jump feel: a short SPACE tap
 * produces a small arc while holding it extends air time. Two short raycasts
 * beneath the player feet detect ground contact reliably without friction hacks.
 * A small debounce window (groundDebounceTime) keeps "on ground" true for a
 * few frames after stepping off a ledge so jumps still register.
 */
public class PlayerController extends Component {
	
	private enum PlayerState{
		Small,
		Big,
		Fire,
		Invincible
	}

	public float walkSpeed = 1.9f;
	public float jumpBoost = 1.0f;
	public float jumpImpulse = 3.0f;
	public float slowDownForce = 0.05f;
	public Vector2f terminalVelocity = new Vector2f(3f, 3.1f);
	
	private PlayerState playerState = PlayerState.Small;
	public transient boolean onGround = false;
	private transient float groundDebounce = 0.0f;
	private transient float groundDebounceTime = 0.1f;
	private transient Rigidbody2D rb;
	private transient StateMachine stateMachine;
	private transient float bigJumpBoostFactor = 1.05f;
	private transient float playerWidth = 100f / 80f;
	private transient int jumpTime = 0;
	private transient Vector2f acceleration = new Vector2f();
	private transient Vector2f velocity = new Vector2f();
	private transient boolean isDead = false;
	private transient int enemyBounce = 0;
	
	@Override
	public void start() {
		rb = gameObject.getComponent(Rigidbody2D.class);
		stateMachine = gameObject.getComponent(StateMachine.class);
		rb.setGravityScale(0.0f);
	}
	
	@Override
	public void update(float dt) {
		if(KeyListener.isKeyPressed(GLFW.GLFW_KEY_RIGHT) || KeyListener.isKeyPressed(GLFW.GLFW_KEY_D)) {
			gameObject.transform.scale.x = playerWidth;
			acceleration.x = walkSpeed;
			stateMachine.trigger("startRunning");
			
			if(velocity.x < 0) {
				velocity.x += slowDownForce;
			}
		} else if(KeyListener.isKeyPressed(GLFW.GLFW_KEY_LEFT) || KeyListener.isKeyPressed(GLFW.GLFW_KEY_A)) {
			gameObject.transform.scale.x = -playerWidth;
			acceleration.x = -walkSpeed;
			stateMachine.trigger("startRunning");
			
			if(velocity.x > 0) {
				velocity.x -= slowDownForce;
			}
		}else {
			acceleration.x = 0;
			if(velocity.x > 0) {
				velocity.x = Math.max(0, velocity.x - slowDownForce);
			}else if(velocity.x < 0) {
				velocity.x = Math.min(0, velocity.x + slowDownForce);
			}
			
			if(velocity.x == 0) {
				stateMachine.trigger("stopRunning");
			}
		}
		
		checkOnGround();
		if(KeyListener.isKeyPressed(GLFW.GLFW_KEY_SPACE) && (jumpTime > 0 || onGround || groundDebounce > 0)) {
			if((onGround || groundDebounce > 0) && jumpTime == 0) {
				// play jump sound
				jumpTime = 60;
				velocity.y = jumpImpulse;
			}else if(jumpTime > 0) {
				jumpTime--;
				velocity.y = ((jumpTime / 2.2f) * jumpBoost);
			}else {
				velocity.y = 0;
			}
			
			groundDebounce = 0;
		}else if(!onGround) {
			if(jumpTime > 0) {
				velocity.y *= 0.35f;
				jumpTime = 0;
			}
			
			groundDebounce -= dt;
			acceleration.y = Window.getPhysics().getGravity().y * 0.7f;
		}else {
			velocity.y = 0;
			acceleration.y = 0;
			groundDebounce = groundDebounceTime;
		}
		
		velocity.x += acceleration.x * dt;
		velocity.y += acceleration.y * dt;
		velocity.x = Math.max(Math.min(velocity.x, terminalVelocity.x), -terminalVelocity.x);
		velocity.y = Math.max(Math.min(velocity.y, terminalVelocity.y), -terminalVelocity.y);
		
		rb.setVelocity(velocity);
		rb.setAngularVelocity(0);
		
		if(!onGround) {
			stateMachine.trigger("jump");
		}else {
			stateMachine.trigger("stopJumping");
		}
	}
	
	public void checkOnGround() {
		Vector2f raycastBegin = new Vector2f(gameObject.transform.position);
		float innerPlayerWidth = playerWidth * 0.35f;
		raycastBegin.sub(innerPlayerWidth / 2.0f, 0.0f);
		float yVal = playerState == PlayerState.Small ? -0.4f : -0.24f;
		Vector2f raycastEnd = new Vector2f(raycastBegin).add(0.0f, yVal);
		
		RaycastInfo info = Window.getPhysics().raycast(gameObject, raycastBegin, raycastEnd);
		
		Vector2f raycast2Begin = new Vector2f(raycastBegin).add(innerPlayerWidth, 0.0f);
		Vector2f raycast2End = new Vector2f(raycastEnd).add(innerPlayerWidth, 0.0f);
		RaycastInfo info2 = Window.getPhysics().raycast(gameObject, raycast2Begin, raycast2End);
		
		onGround = (info.hit && info.hitObject != null && info.hitObject.getComponent(Ground.class) != null ||
				info2.hit && info2.hitObject != null && info2.hitObject.getComponent(Ground.class) != null);
	}
	
	public boolean hasWon() {
		return false;
	}
	
	public void setPosition(Vector2f newPosition) {
		gameObject.transform.position.set(newPosition);
		rb.setPosition(newPosition);
	}
	
	@Override
	public void beginCollision(GameObject collidingObject, Contact contact, Vector2f contactNormal) {
		if(isDead) {
			return;
		}
		
		if(collidingObject.getComponent(Ground.class) != null) {
			if(Math.abs(contactNormal.x) > 0.8f) {
				velocity.x = 0;
			}else if(contactNormal.y > 0.8f) {
				velocity.y = 0;
				acceleration.y = 0;
				jumpTime = 0;
			}
		}
	}
}
