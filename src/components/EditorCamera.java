package components;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import core.Camera;
import core.KeyListener;
import core.MouseListener;

/**
 * Editor-mode camera controller. Middle-mouse drag pans the view; scroll wheel
 * zooms. Press 0 to smoothly lerp back to the origin at zoom 1.
 */
public class EditorCamera extends Component {
	
	private float dragDebounce = 0.032f;

	private Camera levelEditorCamera;
	private Vector2f clickOrigin;
	
	private float lerpTime = 0.0f;
	private float dragSensitivity = 30.0f;
	private float scrollSensitivity = 0.1f;
	
	private boolean reset = false;
	
	public EditorCamera(Camera levelEditorCamera) {
		this.levelEditorCamera = levelEditorCamera;
		this.clickOrigin = new Vector2f();
	}
	
	@Override
	public void editorUpdate(float dt) {
		if(MouseListener.mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_MIDDLE) && dragDebounce > 0) {
			this.clickOrigin = MouseListener.getWorld();
			dragDebounce -= dt;
			return;
		}else if(MouseListener.mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_MIDDLE)) {
			Vector2f mousePos = MouseListener.getWorld();
			Vector2f delta = new Vector2f(mousePos).sub(this.clickOrigin);
			levelEditorCamera.position.sub(delta.mul(dt).mul(dragSensitivity));
			this.clickOrigin.lerp(mousePos, dt);
		}
		
		if(dragDebounce <= 0.0f && !MouseListener.mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_MIDDLE)) {
			dragDebounce = 0.032f;
		}
		
		if(MouseListener.getScrollY() != 0.0f) {
			float addValue = (float)Math.pow(Math.abs(MouseListener.getScrollY() * scrollSensitivity), 1 / levelEditorCamera.getZoom());
			addValue *= -Math.signum(MouseListener.getScrollY());
			levelEditorCamera.addZoom(addValue);
		}
		
		if(KeyListener.isKeyPressed(GLFW.GLFW_KEY_0)) {
			reset = true;
		}
		
		if(reset) {
			levelEditorCamera.position.lerp(new Vector2f(), lerpTime);
			levelEditorCamera.setZoom(this.levelEditorCamera.getZoom() + ((1.0f - levelEditorCamera.getZoom()) * lerpTime));
			this.lerpTime += 0.1f * dt;
			
			if(Math.abs(levelEditorCamera.position.x) <= 5.0f && Math.abs(levelEditorCamera.position.y) <= 5.0f) {
				this.lerpTime = 0.0f;
				levelEditorCamera.position.set(0f, 0f);
				levelEditorCamera.setZoom(1.0f);
				reset = false;
			}
		}
	}
}
