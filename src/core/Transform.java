package core;
import org.joml.Vector2f;

import components.Component;
import editor.AbyssImGui;

/**
 * Component that stores a game object's world-space position, scale, rotation,
 * and z-index. Always present on every GameObject; the renderer uses z-index
 * to sort draw calls.
 */
public class Transform extends Component {

	public Vector2f position;
	public Vector2f scale;
	public float rotation = 0.0f;
	public int zIndex;
	
	public Transform() {
		init(new Vector2f(), new Vector2f());
	}
	
	public Transform(Vector2f position) {
		init(position, new Vector2f());
	}
	
	public Transform(Vector2f position, Vector2f scale) {
		init(position, scale);
	}
	
	public void init(Vector2f position, Vector2f scale) {
		this.position = position;
		this.scale = scale;
		this.zIndex = 0;
	}
	
	@Override
	public void imgui() {
//		gameObject.name = AbyssImGui.inputText("Name: ", gameObject.name);
		AbyssImGui.drawVec2Control("Position", position);
		AbyssImGui.drawVec2Control("Scale", scale, 32.0f);
		rotation = AbyssImGui.dragFloat("Rotation", rotation);
		zIndex = AbyssImGui.dragInt("Z-Index", zIndex);
	}
	
	public Transform copy() {
		return new Transform(new Vector2f(this.position), new Vector2f(this.scale));
	}
	
	public void copy(Transform to) {
		to.position.set(this.position);
		to.scale.set(this.scale);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		
		if(!(o instanceof Transform)) {
			return false;
		}
		
		Transform t = (Transform) o;
		return t.position.equals(this.position) && t.scale.equals(this.scale) && t.rotation == this.rotation && t.zIndex == this.zIndex;
	}
}
