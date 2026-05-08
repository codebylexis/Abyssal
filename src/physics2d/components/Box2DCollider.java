package physics2d.components;

import org.joml.Vector2f;

import components.Component;
import graphics.DebugDraw;

/** Axis-aligned rectangular collider. Draws a debug outline in the editor. */
public class Box2DCollider extends Component {

	private Vector2f halfSize = new Vector2f(1);
	private Vector2f origin = new Vector2f();
	private Vector2f offset = new Vector2f();
	
	public Vector2f getOffset() {
		return offset;
	}

	public Vector2f getHalfSize() {
		return halfSize;
	}

	public void setHalfSize(Vector2f halfSize) {
		this.halfSize = halfSize;
	}
	
	public Vector2f getOrigin() {
		return origin;
	}
	
	public void setOffset(Vector2f newOffset) {
		offset.set(newOffset);
	}
	
	@Override
	public void editorUpdate(float dt) {
		Vector2f center = new Vector2f(gameObject.transform.position).add(offset);
		DebugDraw.addBox2D(center, halfSize, gameObject.transform.rotation);
	}
}
