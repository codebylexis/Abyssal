package components;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import core.GameObject;
import core.MouseListener;
import core.Prefabs;
import core.Window;
import editor.PropertiesWindow;

/**
 * Gizmo that moves the selected object by mapping mouse world-space delta
 * to the active axis. Press W in the editor to activate.
 */
public class TranslateGizmo extends Gizmo {
	
	public TranslateGizmo(Sprite arrowSprite, PropertiesWindow propertiesWindow) {
		super(arrowSprite, propertiesWindow);
	}
	
	@Override
	public void editorUpdate(float dt) {
		if(activeGameObject != null) {
			if(xAxisActive && !yAxisActive) {
				activeGameObject.transform.position.x -= MouseListener.getWorldX();
			}else if(yAxisActive) {
				activeGameObject.transform.position.y -= MouseListener.getWorldY();
			}
		}
		
		super.editorUpdate(dt);
	}
}
