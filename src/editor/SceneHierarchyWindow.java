package editor;

import java.util.List;

import core.GameObject;
import core.Window;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;

/**
 * ImGui panel listing all serializable GameObjects in the active scene.
 * Clicking a row selects it in the PropertiesWindow. Supports drag-and-drop
 * reordering (payload defined but hierarchy reparenting not yet implemented).
 */
public class SceneHierarchyWindow {
	
	private static String payloadDragDropType = "SceneHierarchy";
	
	private PropertiesWindow propertiesWindow;
	
	public SceneHierarchyWindow(PropertiesWindow propertiesWindow) {
		this.propertiesWindow = propertiesWindow;
	}

	public void imgui() {
		ImGui.begin("Scene Hierarchy");
		
		List<GameObject> gameObjects = Window.getScene().getGameObjects();
		int index = 0;
		for(GameObject obj : gameObjects) {
			if(!obj.doSerialization()) {
				continue;
			}
			
			boolean treeNodeOpen = doTreeNode(obj, index);
			
			if(treeNodeOpen) {
				ImGui.treePop();
			}
			
			index++;
		}
		
		ImGui.end();
	}
	
	private boolean doTreeNode(GameObject obj, int index) {
		ImGui.pushID(index);
		boolean treeNodeOpen = ImGui.treeNodeEx(obj.name, ImGuiTreeNodeFlags.DefaultOpen | ImGuiTreeNodeFlags.FramePadding | ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.SpanAvailWidth, obj.name);
		ImGui.popID();
		
		// used for item selection
		if(ImGui.isItemClicked()) {
			// select object in scene hierarchy
			propertiesWindow.setActiveGameObject(obj);
		}
		
		if(ImGui.beginDragDropSource()) {
			ImGui.setDragDropPayload(payloadDragDropType, obj);
			ImGui.text(obj.name);
			ImGui.endDragDropSource();
		}
		
		if(ImGui.beginDragDropTarget()) {
			Object payloadObj = ImGui.acceptDragDropPayload(payloadDragDropType);
			if(payloadObj != null) {
				if(payloadObj.getClass().isAssignableFrom(GameObject.class)) {
					GameObject playerGameObj = (GameObject)payloadObj;
				}
			}
			ImGui.endDragDropTarget();
		}
		
		return treeNodeOpen;
	}
}
