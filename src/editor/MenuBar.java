package editor;

import imgui.ImGui;
import observers.EventSystem;
import observers.events.Event;
import observers.events.EventType;

/** ImGui menu bar with File > Save and File > Load actions. */
public class MenuBar {

	public void imgui() {
		ImGui.beginMenuBar();
		
		if(ImGui.beginMenu("File")) {
			if(ImGui.menuItem("Save", "Ctrl+S")) {
				EventSystem.notify(null, new Event(EventType.SaveLevel));
			}
			
			if(ImGui.menuItem("Load", "Ctrl+O")) {
				EventSystem.notify(null, new Event(EventType.LoadLevel));
			}
			
			ImGui.endMenu();
		}
		
		ImGui.endMenuBar();
	}
}
