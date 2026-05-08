package core;

import components.AnimationState;
import components.PlayerController;
import components.Sprite;
import components.SpriteRenderer;
import components.Spritesheet;
import components.StateMachine;
import physics2d.components.PillboxCollider;
import physics2d.components.Rigidbody2D;
import physics2d.enums.BodyType;
import util.AssetPool;

/**
 * Factory for pre-configured GameObjects. generateSpriteObject creates a
 * generic sized sprite entity. generatePlayer assembles the full player
 * with animation states, a pillbox collider, and a PlayerController.
 */
public class Prefabs {

	public static GameObject generateSpriteObject(Sprite sprite, float sizeX, float sizeY) {
		GameObject block = Window.getScene().createGameObject("sprite_object_gen");
		block.transform.scale.x = sizeX;
		block.transform.scale.y = sizeY;
		SpriteRenderer renderer = new SpriteRenderer();
		renderer.setSprite(sprite);
		block.addComponent(renderer);
		
		return block;
	}
	
	public static GameObject generatePlayer() {
		Spritesheet playerSprites = AssetPool.getSpritesheet("res/textures/spritesheet.png");
		GameObject player = generateSpriteObject(playerSprites.getSprite(0), 100f / 80f, 74f / 80f);
		
		AnimationState idle = new AnimationState();
		idle.title = "Idle";
		float defaultFrameTime = 0.2f;
		idle.addFrame(playerSprites.getSprite(0), defaultFrameTime);
		idle.addFrame(playerSprites.getSprite(1), defaultFrameTime);
		idle.addFrame(playerSprites.getSprite(2), defaultFrameTime);
		idle.addFrame(playerSprites.getSprite(3), defaultFrameTime);
		idle.setLoop(true);
		
		AnimationState run = new AnimationState();
		run.title = "Run";
		run.addFrame(playerSprites.getSprite(8), defaultFrameTime);
		run.addFrame(playerSprites.getSprite(9), defaultFrameTime);
		run.addFrame(playerSprites.getSprite(10), defaultFrameTime);
		run.addFrame(playerSprites.getSprite(11), defaultFrameTime);
		run.addFrame(playerSprites.getSprite(12), defaultFrameTime);
		run.addFrame(playerSprites.getSprite(13), defaultFrameTime);
		run.setLoop(true);
		
		AnimationState jump = new AnimationState();
		jump.title = "Jump";
		jump.addFrame(playerSprites.getSprite(16), defaultFrameTime);
		jump.setLoop(false);
		
		StateMachine stateMachine = new StateMachine();
		stateMachine.addState(idle);
		stateMachine.addState(run);
		stateMachine.addState(jump);
		
		stateMachine.setDefaultState(idle.title);
		stateMachine.addStateTrigger(idle.title, run.title, "startRunning");
		stateMachine.addStateTrigger(run.title, idle.title, "stopRunning");
		stateMachine.addStateTrigger(run.title, jump.title, "jump");
		stateMachine.addStateTrigger(idle.title, jump.title, "jump");
		stateMachine.addStateTrigger(jump.title, idle.title, "stopJumping");
		player.addComponent(stateMachine);
		
		PillboxCollider pb = new PillboxCollider();
		pb.width = 0.39f;
		pb.height = 74f / 80f;
		Rigidbody2D rb = new Rigidbody2D();
		rb.setBodyType(BodyType.Dynamic);
		rb.setContinuousCollision(false);
		rb.setFixedRotation(true);
		rb.setMass(25.0f);
		
		player.addComponent(rb);
		player.addComponent(pb);
		player.addComponent(new PlayerController());
		
		return player;
	}
}
