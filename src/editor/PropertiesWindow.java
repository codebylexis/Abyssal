package editor;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector4f;

import components.SpriteRenderer;
import core.GameObject;
import graphics.PickingTexture;
import imgui.ImGui;
import physics2d.components.Box2DCollider;
import physics2d.components.CircleCollider;
import physics2d.components.Rigidbody2D;

/**
 * ImGui inspector panel for the currently selected GameObject. Shows each
 * component in a collapsing header; right-clicking the panel opens a context
 * menu to add Rigidbody2D, Box2DCollider, or CircleCollider components.
 */
public class PropertiesWindow {

	private List<GameObject> activeGameObjects = null;
	private List<Vector4f> activeGameObjectsOgColor;
	private GameObject activeGameObject = null;
	private PickingTexture pickingTexture;
	
	public PropertiesWindow(PickingTexture pickingTexture) {
		this.pickingTexture = pickingTexture;
		this.activeGameObjects = new ArrayList<>();
		this.activeGameObjectsOgColor = new ArrayList<>();
	}
	
	public void imgui() {
		if(activeGameObjects.size() == 1 && activeGameObjects.get(0) != null) {
			activeGameObject = activeGameObjects.get(0);
			ImGui.begin("Properties");
			
			if(ImGui.beginPopupContextWindow("ComponentAdder")) {
				if(ImGui.menuItem("Add Rigidbody")) {
					if(activeGameObject.getComponent(Rigidbody2D.class) == null) {
						activeGameObject.addComponent(new Rigidbody2D());
					}
				}
				
				if(ImGui.menuItem("Add Box Collider")) {
					if(activeGameObject.getComponent(Box2DCollider.class) == null && activeGameObject.getComponent(CircleCollider.class) == null) {
						activeGameObject.addComponent(new Box2DCollider());
					}
				}
				
				if(ImGui.menuItem("Add Circle Collider")) {
					if(activeGameObject.getComponent(CircleCollider.class) == null && activeGameObject.getComponent(Box2DCollider.class) == null) {
						activeGameObject.addComponent(new CircleCollider());
					}
				}
				
				ImGui.endPopup();
			}
			
			activeGameObject.imgui();
			ImGui.end();
		}
	}
	
	public GameObject getActiveGameObject() {
		return activeGameObjects.size() == 1 ? activeGameObjects.get(0) : null;
	}
	
	public List<GameObject> getActiveGameObjects() {
		return activeGameObjects;
	}
	
	public void clearSelected() {
		if(activeGameObjectsOgColor.size() > 0) {
			int i = 0;
			for(GameObject go : activeGameObjects) {
				SpriteRenderer spr = go.getComponent(SpriteRenderer.class);
				if(spr != null) {
					spr.setColor(activeGameObjectsOgColor.get(i));
				}
				i++;
			}
		}
		activeGameObjects.clear();
		activeGameObjectsOgColor.clear();
	}
	
	public void setActiveGameObject(GameObject go) {
		if(go != null) {
			clearSelected();
			activeGameObjects.add(go);
		}
	}
	
	public void addActiveGameObject(GameObject go) {
		SpriteRenderer spr = go.getComponent(SpriteRenderer.class);
		if(spr != null) {
			activeGameObjectsOgColor.add(new Vector4f(spr.getColor()));
			spr.setColor(new Vector4f(0.8f, 0.8f, 0.0f, 0.8f));
		}else {
			activeGameObjectsOgColor.add(new Vector4f());
		}
		activeGameObjects.add(go);
	}
	
	public PickingTexture getPickingTexture() {
		return pickingTexture;
	}
}
