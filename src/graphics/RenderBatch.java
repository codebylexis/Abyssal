package graphics;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL30;

import components.SpriteRenderer;
import core.GameObject;
import core.Shader;
import core.Texture;
import core.Window;

/**
 * Batches up to maxBatchSize sprites into a single OpenGL draw call. Each
 * vertex carries: position (2), color (4), UV coords (2), texture slot (1),
 * and entity ID (1) -- 10 floats total. Up to 8 texture units are supported
 * per batch; sprites requiring a ninth texture are pushed to a new batch.
 * The entity ID channel is read back by PickingTexture for mouse picking.
 */
public class RenderBatch implements Comparable<RenderBatch> {

	private final int POS_SIZE = 2;
	private final int COLOR_SIZE = 4;
	private final int TEX_COORDS_SIZE = 2;
	private final int TEX_ID_SIZE = 1;
	private final int ENTITY_ID_SIZE = 1;
	
	private final int POS_OFFSET = 0;
	private final int COLOR_OFFSET = POS_OFFSET + POS_SIZE * Float.BYTES;
	private final int TEX_COORDS_OFFSET = COLOR_OFFSET + COLOR_SIZE * Float.BYTES;
	private final int TEX_ID_OFFSET = TEX_COORDS_OFFSET + TEX_COORDS_SIZE * Float.BYTES;
	private final int ENTITY_ID_OFFSET = TEX_ID_OFFSET + TEX_ID_SIZE * Float.BYTES;
	private final int VERTEX_SIZE = 10;
	private final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;
	
	private SpriteRenderer[] sprites;
	private int numSprites;
	private boolean hasRoom;
	private float[] vertices;
	private List<Texture> textures;
	private int[] texSlots = {0, 1, 2, 3, 4, 5, 6, 7};
	
	private int vaoId, vboId;
	private int maxBatchSize;
	private int zIndex;
	
	private Renderer renderer;
	
	public RenderBatch(int maxBatchSize, int zIndex, Renderer renderer) {
		this.zIndex = zIndex;
		this.sprites = new SpriteRenderer[maxBatchSize];
		this.maxBatchSize = maxBatchSize;
		this.renderer = renderer;
		
		vertices = new float[maxBatchSize * 4 * VERTEX_SIZE];
		
		this.numSprites = 0;
		this.hasRoom = true;
		this.textures = new ArrayList<Texture>();
	}
	
	public void start() {
		vaoId = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vaoId);
		
		vboId = GL30.glGenBuffers();
		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboId);
		GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertices.length * Float.BYTES, GL30.GL_DYNAMIC_DRAW);
		
		int eboId = GL30.glGenBuffers();
		int[] indices = generateIndices();
		GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, eboId);
		GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indices, GL30.GL_STATIC_DRAW);
		
		GL30.glVertexAttribPointer(0, POS_SIZE, GL30.GL_FLOAT, false, VERTEX_SIZE_BYTES, POS_OFFSET);
		GL30.glEnableVertexAttribArray(0);
		
		GL30.glVertexAttribPointer(1, COLOR_SIZE, GL30.GL_FLOAT, false, VERTEX_SIZE_BYTES, COLOR_OFFSET);
		GL30.glEnableVertexAttribArray(1);
		
		GL30.glVertexAttribPointer(2, TEX_COORDS_SIZE, GL30.GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_COORDS_OFFSET);
		GL30.glEnableVertexAttribArray(2);
		
		GL30.glVertexAttribPointer(3, TEX_ID_SIZE, GL30.GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_ID_OFFSET);
		GL30.glEnableVertexAttribArray(3);
		
		GL30.glVertexAttribPointer(4, ENTITY_ID_SIZE, GL30.GL_FLOAT, false, VERTEX_SIZE_BYTES, ENTITY_ID_OFFSET);
		GL30.glEnableVertexAttribArray(4);
	}
	
	public void addSprite(SpriteRenderer spr) {
		int index = this.numSprites;
		this.sprites[index] = spr;
		this.numSprites++;
		
		if(spr.getTexture() != null) {
			if(!textures.contains(spr.getTexture())) {
				textures.add(spr.getTexture());
			}
		}
		
		loadVertexProperties(index);
		
		if(numSprites >= this.maxBatchSize) {
			this.hasRoom = false;
		}
	}
	
	public void render() {
		boolean rebufferData = false;
		for(int i = 0; i < numSprites; i++) {
			SpriteRenderer spr = sprites[i];
			if(spr.isDirty()) {
				if(!hasTexture(spr.getTexture())) {
					renderer.destroyGameObject(spr.gameObject);
					renderer.add(spr.gameObject);
				}else {
					loadVertexProperties(i);
					spr.setClean();
					rebufferData = true;
				}
			}
			
			if(spr.gameObject.transform.zIndex != zIndex) {
				destroyIfExists(spr.gameObject);
				renderer.add(spr.gameObject);
				i--;
			}
		}
		
		if(rebufferData) {
			GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboId);
			GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, 0, vertices);
		}
		
		Shader shader = Renderer.getBoundShader();
		shader.uploadMat4("uProjection", Window.getScene().camera().getProjectionMatrix());
		shader.uploadMat4("uView", Window.getScene().camera().getViewMatrix());
		for(int i = 0; i < textures.size(); i++) {
			GL30.glActiveTexture(GL30.GL_TEXTURE0 + i + 1);
			textures.get(i).bind();
		}
		shader.uploadIntArray("uTextures", texSlots);
		
		GL30.glBindVertexArray(vaoId);
		GL30.glEnableVertexAttribArray(0);
		GL30.glEnableVertexAttribArray(1);
		
		GL30.glDrawElements(GL30.GL_TRIANGLES, this.numSprites * 6, GL30.GL_UNSIGNED_INT, 0);
		
		GL30.glDisableVertexAttribArray(1);
		GL30.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		
		
		for(int i = 0; i < textures.size(); i++) {
			textures.get(i).unbind();
		}
		
		shader.detach();
	}
	
	private void loadVertexProperties(int index) {
		SpriteRenderer sprite = this.sprites[index];
		
		int offset = index * 4 * VERTEX_SIZE;
		
		Vector4f color = sprite.getColor();
		Vector2f[] texCoords = sprite.getTexCoords();
		
		int texId = 0;
		if(sprite.getTexture() != null) {
			for(int i = 0; i < textures.size(); i++) {
				if(textures.get(i).equals(sprite.getTexture())) {
					texId = i + 1;
					break;
				}
			}
		}
		
		boolean isRotated = sprite.gameObject.transform.rotation != 0.0f;
		Matrix4f transformMatrix = new Matrix4f().identity();
		if(isRotated) {
			transformMatrix.translate(sprite.gameObject.transform.position.x, sprite.gameObject.transform.position.y, 0);
			transformMatrix.rotate((float)Math.toRadians(sprite.gameObject.transform.rotation), 0, 0, 1);
			transformMatrix.scale(sprite.gameObject.transform.scale.x, sprite.gameObject.transform.scale.y, 1);
		}
		
		float xAdd = 0.5f;
		float yAdd = 0.5f;
		for(int i = 0; i < 4; i++) {
			if(i == 1) {
				yAdd = -0.5f;
			}else if(i == 2) {
				xAdd = -0.5f;
			}else if(i == 3) {
				yAdd = 0.5f;
			}
			
			Vector4f currentPos = new Vector4f(sprite.gameObject.transform.position.x + (xAdd * sprite.gameObject.transform.scale.x), sprite.gameObject.transform.position.y + (yAdd * sprite.gameObject.transform.scale.y), 0, 1);
			
			if(isRotated) {
				currentPos = new Vector4f(xAdd, yAdd, 0, 1).mul(transformMatrix);
			}
			
			vertices[offset] = currentPos.x;
			vertices[offset + 1] = currentPos.y;
			
			vertices[offset + 2] = color.x;
			vertices[offset + 3] = color.y;
			vertices[offset + 4] = color.z;
			vertices[offset + 5] = color.w;
			
			vertices[offset + 6] = texCoords[i].x;
			vertices[offset + 7] = texCoords[i].y;
			
			vertices[offset + 8] = texId;
			
			vertices[offset + 9] = sprite.gameObject.getUId() + 1;
			
			offset += VERTEX_SIZE;
		}
	}
	
	private int[] generateIndices() {
		int[] elements = new int[6 * maxBatchSize];
		for(int i = 0; i < maxBatchSize; i++) {
			loadElementIndices(elements, i);
		}
		
		return elements;
	}
	
	private void loadElementIndices(int[] elements, int index) {
		int offsetArrayIndex = 6 * index;
		int offset = 4 * index;
		
		elements[offsetArrayIndex] = offset + 3;
		elements[offsetArrayIndex + 1] = offset + 2;
		elements[offsetArrayIndex + 2] = offset + 0;
		
		elements[offsetArrayIndex + 3] = offset + 0;
		elements[offsetArrayIndex + 4] = offset + 2;
		elements[offsetArrayIndex + 5] = offset + 1;
	}
	
	public boolean destroyIfExists(GameObject go) {
		SpriteRenderer sprite = go.getComponent(SpriteRenderer.class);
		for(int i = 0; i < numSprites; i++) {
			if(sprites[i] == sprite) {
				for(int j = i; j < numSprites - 1; j++) {
					sprites[j] = sprites[j + 1];
					sprites[j].setDirty();
				}
				
				numSprites--;
				return true;
			}
		}
		
		return false;
	}
	
	public boolean hasRoom() {
		return this.hasRoom;
	}
	
	public boolean hasTextureRoom() {
		return this.textures.size() < 8;
	}
	
	public boolean hasTexture(Texture tex) {
		return this.textures.contains(tex);
	}
	
	public int zIndex() {
		return this.zIndex;
	}

	@Override
	public int compareTo(RenderBatch o) {
		return Integer.compare(this.zIndex, o.zIndex());
	}
}
