package components;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import core.GameObject;
import core.KeyListener;
import core.MouseListener;
import core.Prefabs;
import core.Window;
import editor.PropertiesWindow;

/**
 * Base class for editor transform handles. Spawns two arrow GameObjects (one
 * per axis) and tracks which axis is being dragged. Subclasses implement the
 * actual transform modification in their editorUpdate override.
 */
public class Gizmo extends Component {

	private Vector4f xAxisColor = new Vector4f(1, 0.3f, 0.3f, 1);
	private Vector4f xAxisColorHover = new Vector4f(1, 0, 0, 1);
	private Vector4f yAxisColor = new Vector4f(0.3f, 1, 0.3f, 1);
	private Vector4f yAxisColorHover = new Vector4f(0, 1, 0, 1);
	
	private GameObject xAxisObject;
	private GameObject yAxisObject;
	private SpriteRenderer xAxisSprite;
	private SpriteRenderer yAxisSprite;
	protected GameObject activeGameObject = null;
	
	private Vector2f xAxisOffset = new Vector2f(24f / 80f, -6f / 80f);
	private Vector2f yAxisOffset = new Vector2f(-7f / 80f, 21f / 80f);
	
	private float gizmoWidth = 16f / 80f;
	private float gizmoHeight = 48f / 80f;
	
	protected boolean xAxisActive = false;
	protected boolean yAxisActive = false;
	
	private boolean using = false;
	
	private PropertiesWindow propertiesWindow;
	
	public Gizmo(Sprite arrowSprite, PropertiesWindow propertiesWindow) {
		this.propertiesWindow = propertiesWindow;
		
		xAxisObject = Prefabs.generateSpriteObject(arrowSprite, gizmoWidth, gizmoHeight);
		yAxisObject = Prefabs.generateSpriteObject(arrowSprite, gizmoWidth, gizmoHeight);
		xAxisSprite = xAxisObject.getComponent(SpriteRenderer.class);
		yAxisSprite = yAxisObject.getComponent(SpriteRenderer.class);
		
		xAxisObject.addComponent(new NonPickable());
		yAxisObject.addComponent(new NonPickable());
		
		Window.getScene().addGameObjectToScene(xAxisObject);
		Window.getScene().addGameObjectToScene(yAxisObject);
	}
	
	@Override
	public void start() {
		xAxisObject.transform.rotation = 90;
		yAxisObject.transform.rotation = 180;
		xAxisObject.transform.zIndex = 100;
		yAxisObject.transform.zIndex = 100;
		xAxisObject.setNoSerialize();
		yAxisObject.setNoSerialize();
	}
	
	@Override
	public void update(float dt) {
		if(using) {
			setInactive();
		}
		
		xAxisObject.getComponent(SpriteRenderer.class).setColor(new Vector4f(0, 0, 0, 0));
		yAxisObject.getComponent(SpriteRenderer.class).setColor(new Vector4f(0, 0, 0, 0));
	}
	
	@Override
	public void editorUpdate(float dt) {
		if(!using) {
			return;
		}
		
		activeGameObject = propertiesWindow.getActiveGameObject();
		if(activeGameObject != null) {
			setActive();
		}else {
			setInactive();
			return;
		}
		
		boolean xAxisHot = checkXHoverState();
		boolean yAxisHot = checkYHoverState();
		
		if((xAxisHot || xAxisActive) && MouseListener.isDragging() && MouseListener.mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
			xAxisActive = true;
			yAxisActive = false;
		}else if((yAxisHot || yAxisActive) && MouseListener.isDragging() && MouseListener.mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
			yAxisActive = true;
			xAxisActive = false;
		}else {
			xAxisActive = false;
			yAxisActive = false;
		}
		
		if(activeGameObject != null) {
			xAxisObject.transform.position.set(activeGameObject.transform.position);
			yAxisObject.transform.position.set(activeGameObject.transform.position);
			xAxisObject.transform.position.add(xAxisOffset);
			yAxisObject.transform.position.add(yAxisOffset);
		}
	}
	
	private void setActive() {
		xAxisSprite.setColor(xAxisColor);
		yAxisSprite.setColor(yAxisColor);
	}
	
	private void setInactive() {
		xAxisSprite.setColor(new Vector4f(0, 0, 0, 0));
		yAxisSprite.setColor(new Vector4f(0, 0, 0, 0));
	}
	
	private boolean checkXHoverState() {
		Vector2f mousePos = MouseListener.getWorld();
		if(mousePos.x <= xAxisObject.transform.position.x + (gizmoHeight / 2.0f) && mousePos.x >= xAxisObject.transform.position.x - (gizmoWidth / 2.0f) && mousePos.y >= xAxisObject.transform.position.y - (gizmoHeight / 2.0f) && mousePos.y <= xAxisObject.transform.position.y + (gizmoWidth / 2.0f)) {
			xAxisSprite.setColor(xAxisColorHover);
			return true;
		}
		
		xAxisSprite.setColor(xAxisColor);
		return false;
	}
	
	private boolean checkYHoverState() {
		Vector2f mousePos = MouseListener.getWorld();
		if(mousePos.x <= yAxisObject.transform.position.x + (gizmoWidth / 2.0f) && mousePos.x >= yAxisObject.transform.position.x - (gizmoWidth / 2.0f) && mousePos.y <= yAxisObject.transform.position.y + (gizmoHeight / 2.0f) && mousePos.y >= yAxisObject.transform.position.y - (gizmoHeight / 2.0f)) {
			yAxisSprite.setColor(yAxisColorHover);
			return true;
		}
		
		yAxisSprite.setColor(yAxisColor);
		return false;
	}
	
	public void setUsing() {
		using = true;
	}
	
	public void setNotUsing() {
		using = false;
		setInactive();
	}
}
