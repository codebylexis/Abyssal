package components;

import org.joml.Vector4f;

import core.Camera;
import core.GameObject;
import core.Window;

/**
 * Camera controller for gameplay mode. Follows the player horizontally while
 * never scrolling left past the furthest-right position reached. Switches
 * the clear color from sky blue to black when the player goes underground.
 */
public class GameCamera extends Component{

	private transient GameObject player;
	private transient Camera gameCamera;
	private transient float highestX = Float.MIN_VALUE;
	private transient float undergroundYLevel = 0.0f;
	private transient float cameraBuffer = 1.5f;
	private transient float playerBuffer = 0.25f;
	
	private Vector4f skyColor = new Vector4f(92.0f / 255.0f, 148.0f / 255.0f, 252.0f / 255.0f, 1.0f);
	private Vector4f undergroundColor = new Vector4f(0, 0, 0, 1);
	
	public GameCamera(Camera gameCamera) {
		this.gameCamera = gameCamera;
	}
	
	@Override
	public void start() {
		player = Window.getScene().getGameObjectWith(PlayerController.class);
		gameCamera.clearColor.set(skyColor);
		undergroundYLevel = gameCamera.position.y - gameCamera.getProjectionSize().y - cameraBuffer;
	}
	
	@Override
	public void update(float dt) {
		if(player != null && !player.getComponent(PlayerController.class).hasWon()) {
			gameCamera.position.x = Math.max(player.transform.position.x - 2.5f, highestX);
			highestX = Math.max(highestX, gameCamera.position.x);
			
			if(player.transform.position.y < -playerBuffer) {
				gameCamera.position.y = undergroundYLevel;
				gameCamera.clearColor.set(undergroundColor);
			}else if(player.transform.position.y >= 0.0f) {
				gameCamera.position.y = 0;
				gameCamera.clearColor.set(skyColor);
			}
		}
	}
}
