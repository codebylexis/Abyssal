package components;

import java.util.HashSet;
import java.util.Set;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import core.GameObject;
import core.KeyListener;
import core.MouseListener;
import core.Window;
import editor.PropertiesWindow;
import graphics.DebugDraw;
import graphics.PickingTexture;
import scenes.Scene;
import util.Settings;

/**
 * Handles all mouse-driven editor interactions: placing a held tile/object on
 * left-click, single-click picking via the GPU picking texture, and rubber-band
 * box selection. Objects are snapped to the grid during placement.
 */
public class MouseControls extends Component {

	public GameObject holdingObject = null;
	
	private float debounceTime = 0.2f;
	private float debounce = debounceTime;
	
	private boolean boxSelectSet = false;
	private Vector2f boxSelectStart = new Vector2f();
	private Vector2f boxSelectEnd = new Vector2f();
	
	public void pickupObject(GameObject go) {
		if(holdingObject != null) {
			holdingObject.destroy();
		}
		holdingObject = go;
		holdingObject.getComponent(SpriteRenderer.class).setColor(new Vector4f(0.8f, 0.8f, 0.8f, 0.5f));
		holdingObject.addComponent(new NonPickable());
		Window.getScene().addGameObjectToScene(go);
	}
	
	public void place() {
		GameObject newObj = holdingObject.copy();
		if(newObj.getComponent(StateMachine.class) != null) {
			newObj.getComponent(StateMachine.class).refreshTextures();
		}
		newObj.getComponent(SpriteRenderer.class).setColor(new Vector4f(1, 1, 1, 1));
		newObj.removeComponent(NonPickable.class);
		Window.getScene().addGameObjectToScene(newObj);
	}
	
	@Override
	public void editorUpdate(float dt) {
		debounce -= dt;
		PickingTexture pickingTexture = Window.getImGuiLayer().getPropertiesWindow().getPickingTexture();
		Scene currentScene = Window.getScene();
		
		if(holdingObject != null) {
			holdingObject.transform.position.x = MouseListener.getWorldX();
			holdingObject.transform.position.y = MouseListener.getWorldY();
			holdingObject.transform.position.x = ((int)Math.floor(holdingObject.transform.position.x / Settings.GRID_WIDTH) * Settings.GRID_WIDTH) + Settings.GRID_WIDTH / 2.0f;
			holdingObject.transform.position.y = ((int)Math.floor(holdingObject.transform.position.y / Settings.GRID_HEIGHT) * Settings.GRID_HEIGHT) + Settings.GRID_HEIGHT / 2.0f;
			
			if(MouseListener.mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
				float halfWidth = Settings.GRID_WIDTH / 2.0f;
				float halfHeight = Settings.GRID_HEIGHT / 2.0f;
				if(MouseListener.isDragging() && !blockInSquare(holdingObject.transform.position.x - halfWidth, holdingObject.transform.position.y - halfHeight)) {
					place();
				}else if(!MouseListener.isDragging() && debounce < 0) {
					place();
					debounce = debounceTime;
				}
			}
			
			if(KeyListener.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
				holdingObject.destroy();
				holdingObject = null;
			}
		}else if(!MouseListener.isDragging() && MouseListener.mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT) && debounce < 0) {
			int x = (int)MouseListener.getScreenX();
			int y = (int)MouseListener.getScreenY();
			int gameObjectId = pickingTexture.readPixel(x, y);
			GameObject pickedObject = currentScene.getGameObject(gameObjectId);
			if(pickedObject != null && pickedObject.getComponent(NonPickable.class) == null) {
				Window.getImGuiLayer().getPropertiesWindow().setActiveGameObject(pickedObject);
			}else if(pickedObject == null && !MouseListener.isDragging()) {
				Window.getImGuiLayer().getPropertiesWindow().clearSelected();
			}
			debounce = 0.2f;
		}else if(MouseListener.isDragging() && MouseListener.mouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
			if(!boxSelectSet) {
				Window.getImGuiLayer().getPropertiesWindow().clearSelected();
				boxSelectStart = MouseListener.getScreen();
				boxSelectSet = true;
			}
			
			boxSelectEnd = MouseListener.getScreen();
			Vector2f boxSelectStartWorld = MouseListener.screenToWorld(boxSelectStart);
			Vector2f boxSelectEndWorld = MouseListener.screenToWorld(boxSelectEnd);
			Vector2f halfSize = (new Vector2f(boxSelectEndWorld).sub(boxSelectStartWorld)).mul(0.5f);
			DebugDraw.addBox2D((new Vector2f(boxSelectStartWorld)).add(halfSize), new Vector2f(halfSize).mul(2.0f), 0.0f);
		}else if(boxSelectSet) {
			boxSelectSet = false;
			int screenStartX = (int)boxSelectStart.x;
			int screenStartY = (int)boxSelectStart.y;
			int screenEndX = (int)boxSelectEnd.x;
			int screenEndY = (int)boxSelectEnd.y;
			boxSelectStart.zero();
			boxSelectEnd.zero();
			
			if(screenEndX < screenStartX) {
				int tmp = screenStartX;
				screenStartX = screenEndX;
				screenEndX = tmp;
			}
			
			if(screenEndY < screenStartY) {
				int tmp = screenStartY;
				screenStartY = screenEndY;
				screenEndY = tmp;
			}
			
			float[] gameObjectIds = pickingTexture.readPixels(new Vector2i(screenStartX, screenStartY), new Vector2i(screenEndX, screenEndY));
			Set<Integer> uniqueGameObjectIds = new HashSet<>();
			for(float objId : gameObjectIds) {
				uniqueGameObjectIds.add((int)objId);
			}
			
			for(Integer gameObjectId : uniqueGameObjectIds) {
				GameObject pickedObject = Window.getScene().getGameObject(gameObjectId);
				if(pickedObject != null && pickedObject.getComponent(NonPickable.class) == null) {
					Window.getImGuiLayer().getPropertiesWindow().addActiveGameObject(pickedObject);
				}
			}
		}
	}
	
	private boolean blockInSquare(float x, float y) {
		PropertiesWindow propertiesWindow = Window.getImGuiLayer().getPropertiesWindow();
		Vector2f start = new Vector2f(x, y);
		Vector2f end = new Vector2f(start).add(new Vector2f(Settings.GRID_WIDTH, Settings.GRID_HEIGHT));
		Vector2f startScreenf = MouseListener.worldToScreen(start);
		Vector2f endScreenf = MouseListener.worldToScreen(end);
		Vector2i startScreen = new Vector2i((int)startScreenf.x + 2, (int)startScreenf.y + 2);
		Vector2i endScreen = new Vector2i((int)endScreenf.x - 2, (int)endScreenf.y - 2);
		float[] gameObjectIds = propertiesWindow.getPickingTexture().readPixels(startScreen, endScreen);
		
		for(int i = 0; i < gameObjectIds.length; i++) {
			if(gameObjectIds[i] >= 0) {
				GameObject pickedObject = Window.getScene().getGameObject((int)gameObjectIds[i]);
				
				if(pickedObject.getComponent(NonPickable.class) == null) {
					return true;
				}
			}
		}
		
		return false;
	}
}
