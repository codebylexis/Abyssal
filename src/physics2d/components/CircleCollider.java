package physics2d.components;

import org.joml.Vector2f;

import components.Component;
import graphics.DebugDraw;

/** Circular collider. Draws a debug circle in the editor. */
public class CircleCollider extends Component {
	
	protected Vector2f offset = new Vector2f();

	private float radius = 1;

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}
	
	public Vector2f getOffset() {
		return offset;
	}
	
	public void setOffset(Vector2f newOffset) {
		offset.set(newOffset);
	}
	
	@Override
	public void editorUpdate(float dt) {
		Vector2f center = new Vector2f(gameObject.transform.position).add(offset);
		DebugDraw.addCircle(center, radius);
	}
}
