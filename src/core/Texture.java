package core;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;;

/**
 * Loads an image file with STB and uploads it as an OpenGL 2D texture. Supports
 * RGB and RGBA formats. The constructor path is used as an equality key so the
 * same file is never uploaded twice when compared through AssetPool.
 */
public class Texture {

	private String filePath;
	private transient int textureId;
	private int width;
	private int height;
	
	public Texture() {
		textureId = -1;
		width = -1;
		height = -1;
	}
	
	public Texture(int width, int height) {
		this.filePath = "Generated";
		
		textureId = GL20.glGenTextures();
		GL20.glBindTexture(GL20.GL_TEXTURE_2D, textureId);
		
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
		
		GL20.glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL20.GL_RGB, width, height, 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, 0);
	}
	
	public void init(String filePath) {
		this.filePath = filePath;
		
		textureId = GL20.glGenTextures();
		GL20.glBindTexture(GL20.GL_TEXTURE_2D, textureId);
		
		GL20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, GL20.GL_REPEAT);
		GL20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T, GL20.GL_REPEAT);
		GL20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_NEAREST);
		GL20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_NEAREST);
		
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		IntBuffer channels = BufferUtils.createIntBuffer(1);
		STBImage.stbi_set_flip_vertically_on_load(true);
		ByteBuffer image = STBImage.stbi_load(filePath, width, height, channels, 0);
		
		if(image != null) {
			this.width = width.get(0);
			this.height = height.get(0);
			
			if(channels.get(0) == 3) {
				GL20.glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL20.GL_RGB, width.get(0), height.get(0), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, image);
			}else if(channels.get(0) == 4) {
				GL20.glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL20.GL_RGBA, width.get(0), height.get(0), 0, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, image);
			}else {
				assert false : "Error: unknown number of channels";
			}
		}else {
			assert false : "Error: could not load image: " + filePath;
		}
		
		STBImage.stbi_image_free(image);
	}
	
	public void bind() {
		GL20.glBindTexture(GL20.GL_TEXTURE_2D, textureId);
	}
	
	public void unbind() {
		GL20.glBindTexture(GL20.GL_TEXTURE_2D, 0);
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public int getId() {
		return textureId;
	}
	
	public String getFilepath() {
		return this.filePath;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		
		if(!(o instanceof Texture)) {
			return false;
		}
		
		Texture oTex = (Texture)o;
		
		return oTex.getWidth() == this.width && oTex.getHeight() == this.height && oTex.getId() == this.textureId && oTex.getFilepath().equals(this.filePath);
	}
}
