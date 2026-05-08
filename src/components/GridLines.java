package components;

import org.joml.Vector2f;
import org.joml.Vector3f;

import core.Camera;
import core.Window;
import graphics.DebugDraw;
import util.Settings;

/** Draws a debug grid aligned to the tile size, covering the visible viewport. */
public class GridLines extends Component {

	@Override
	public void editorUpdate(float delta) {
		Camera camera = Window.getScene().camera();
		Vector2f cameraPos = camera.position;
		Vector2f projectionSize = camera.getProjectionSize();
		
		float firstX = ((int)(cameraPos.x / Settings.GRID_WIDTH) - 1) * Settings.GRID_HEIGHT;
		float firstY = ((int)(cameraPos.y / Settings.GRID_HEIGHT) - 1) * Settings.GRID_HEIGHT;
		
		int numVtLines = (int)(projectionSize.x * camera.getZoom() / Settings.GRID_WIDTH) + 2;
		int numHzLines = (int)(projectionSize.y * camera.getZoom() / Settings.GRID_HEIGHT) + 2;
		
		float height = (int)(projectionSize.y * camera.getZoom()) + (5 * Settings.GRID_HEIGHT);
		float width = (int)(projectionSize.x * camera.getZoom()) + (5 * Settings.GRID_WIDTH);
		
		int maxLines = Math.max(numVtLines, numHzLines);
		Vector3f color = new Vector3f(0.2f, 0.2f, 0.2f);
		for(int i = 0; i < maxLines; i++) {
			float x = firstX + (Settings.GRID_WIDTH * i);
			float y = firstY + (Settings.GRID_HEIGHT * i);
			
			if(i < numVtLines) {
				DebugDraw.addLine2D(new Vector2f(x, firstY), new Vector2f(x, firstY + height), color);
			}
			
			if(i < numHzLines) {
				DebugDraw.addLine2D(new Vector2f(firstX, y), new Vector2f(firstX + width, y), color);
			}
		}
	}
}
