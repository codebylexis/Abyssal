package graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import components.SpriteRenderer;
import core.GameObject;
import core.Shader;
import core.Texture;

/**
 * Manages a z-index-sorted list of RenderBatches. Routes each SpriteRenderer
 * to an existing batch that has room and shares the same z-index and texture
 * set, or opens a new batch when none qualifies. The active shader is stored
 * statically so RenderBatch can retrieve it without a reference.
 */
public class Renderer {

	private final int MAX_BATCH_SIZE = 1000;
	private List<RenderBatch> batches;
	private static Shader currentShader;
	
	public Renderer() {
		this.batches = new ArrayList<RenderBatch>();
	}
	
	public void render() {
		currentShader.use();
		for(int i = 0; i < batches.size(); i++) {
			RenderBatch batch = batches.get(i);
			batch.render();
		}
	}
	
	public void add(GameObject go) {
		SpriteRenderer spr = go.getComponent(SpriteRenderer.class);
		if(spr != null) {
			add(spr);
		}
	}
	
	private void add(SpriteRenderer sprite) {
		boolean added = false;
		for(RenderBatch batch : batches) {
			if(batch.hasRoom() && batch.zIndex() == sprite.gameObject.transform.zIndex) {
				Texture tex = sprite.getTexture();
				if(tex == null || (batch.hasTexture(tex) || batch.hasTextureRoom())) {
					batch.addSprite(sprite);
					added = true;
					break;
				}
			}
		}
		
		if(!added) {
			RenderBatch newBatch = new RenderBatch(MAX_BATCH_SIZE, sprite.gameObject.transform.zIndex, this);
			newBatch.start();
			batches.add(newBatch);
			newBatch.addSprite(sprite);
			Collections.sort(batches);
		}
	}
	
	public void destroyGameObject(GameObject go) {
		if(go.getComponent(SpriteRenderer.class) == null) {
			return;
		}
		
		for(RenderBatch batch : batches) {
			if(batch.destroyIfExists(go)) {
				return;
			}
		}
	}
	
	public static Shader getBoundShader() {
		return currentShader;
	}
	
	public static void bindShader(Shader shader) {
		currentShader = shader;
	}
}
