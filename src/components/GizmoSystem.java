package components;

import org.lwjgl.glfw.GLFW;

import core.KeyListener;
import core.Window;

/**
 * Manages which editor gizmo is active. Press E for translate (default),
 * R for scale. Only one gizmo is active at a time.
 */
public class GizmoSystem extends Component {

	private Spritesheet gizmos;
	private int usingGizmo = 0;
	
	public GizmoSystem(Spritesheet gizmoSprites) {
		gizmos = gizmoSprites;
	}
	
	@Override
	public void start() {
		gameObject.addComponent(new TranslateGizmo(gizmos.getSprite(1), Window.getImGuiLayer().getPropertiesWindow()));
		gameObject.addComponent(new ScaleGizmo(gizmos.getSprite(2), Window.getImGuiLayer().getPropertiesWindow()));
	}
	
	@Override
	public void editorUpdate(float dt) {
		if(usingGizmo == 0) {
			gameObject.getComponent(TranslateGizmo.class).setUsing();
			gameObject.getComponent(ScaleGizmo.class).setNotUsing();
		}else if(usingGizmo == 1) {
			gameObject.getComponent(TranslateGizmo.class).setNotUsing();
			gameObject.getComponent(ScaleGizmo.class).setUsing();
		}
		
		if(KeyListener.isKeyPressed(GLFW.GLFW_KEY_E)) {
			usingGizmo = 0;
		}else if(KeyListener.isKeyPressed(GLFW.GLFW_KEY_R)) {
			usingGizmo = 1;
		}
	}
}
