package components;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import core.GameObject;
import core.KeyListener;
import core.Window;
import editor.PropertiesWindow;
import util.Settings;

/**
 * Editor keyboard shortcuts: Ctrl+D duplicates the selected object(s) one
 * grid cell to the right, and Delete removes them from the scene.
 */
public class KeyControls extends Component {
	
	@Override
	public void editorUpdate(float dt) {
		PropertiesWindow propertiesWindow = Window.getImGuiLayer().getPropertiesWindow();
		GameObject activeGameObject = propertiesWindow.getActiveGameObject();
		List<GameObject> activeGameObjects = propertiesWindow.getActiveGameObjects();
		
		if(KeyListener.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL) && KeyListener.keyBeginPress(GLFW.GLFW_KEY_D) && activeGameObject != null) {
			GameObject newObj = activeGameObject.copy();
			Window.getScene().addGameObjectToScene(newObj);
			newObj.transform.position.add(Settings.GRID_WIDTH, 0.0f);
			propertiesWindow.setActiveGameObject(newObj);
			if(newObj.getComponent(StateMachine.class) != null) {
				newObj.getComponent(StateMachine.class).refreshTextures();
			}
		}else if(KeyListener.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL) && KeyListener.keyBeginPress(GLFW.GLFW_KEY_D) && activeGameObjects.size() > 1) {
			List<GameObject> gameObjects = new ArrayList<>(activeGameObjects);
			propertiesWindow.clearSelected();
			for(GameObject go : gameObjects) {
				GameObject copy = go.copy();
				Window.getScene().addGameObjectToScene(copy);
				propertiesWindow.addActiveGameObject(copy);
				if(copy.getComponent(StateMachine.class) != null) {
					copy.getComponent(StateMachine.class).refreshTextures();
				}
			}
		}else if(KeyListener.keyBeginPress(GLFW.GLFW_KEY_DELETE)) {
			for(GameObject go : activeGameObjects) {
				go.destroy();
			}
			
			propertiesWindow.clearSelected();
		}
	}
}
