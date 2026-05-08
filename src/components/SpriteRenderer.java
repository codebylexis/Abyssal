package components;
import org.joml.Vector2f;
import org.joml.Vector4f;

import core.Texture;
import core.Transform;
import editor.AbyssImGui;
import imgui.ImGui;

/**
 * Renders a Sprite at the owning object's Transform. Marks itself dirty when
 * the transform changes or the sprite/color is updated so RenderBatch only
 * re-uploads vertex data to the GPU when something actually changed.
 */
public class SpriteRenderer extends Component {

	private Vector4f color = new Vector4f(1, 1, 1, 1);
	private Sprite sprite = new Sprite();

	private transient Transform lastTransform;
	private transient boolean isDirty = true;
	
	@Override
	public void start() {
		this.lastTransform = gameObject.transform.copy();
	}
	
	@Override
	public void editorUpdate(float delta) {
		if(!this.lastTransform.equals(this.gameObject.transform)) {
			this.gameObject.transform.copy(this.lastTransform);
			isDirty = true;
		}
	}
	
	@Override
	public void update(float delta) {
		if(!this.lastTransform.equals(this.gameObject.transform)) {
			this.gameObject.transform.copy(this.lastTransform);
			isDirty = true;
		}
	}
	
	@Override
	public void imgui() {
		if(AbyssImGui.colorPicker4("Color Picker", color)) {
			this.isDirty = true;
		}
	}
	
	public void setDirty() {
		isDirty = true;
	}
	
	public Vector4f getColor() {
		return this.color;
	}
	
	public Texture getTexture() {
		return sprite.getTexture();
	}
	
	public Vector2f[] getTexCoords() {
		return sprite.getTexCoords();
	}
	
	public void setSprite(Sprite sprite) {
		this.sprite = sprite;
		this.isDirty = true;
	}
	
	public void setColor(Vector4f color) {
		if(!this.color.equals(color)) {
			this.isDirty = true;
			this.color.set(color);
		}
	}
	
	public boolean isDirty() {
		return this.isDirty;
	}
	
	public void setClean() {
		this.isDirty = false;
	}
	
	public void setTexture(Texture texture) {
		this.sprite.setTexture(texture);
	}
}
