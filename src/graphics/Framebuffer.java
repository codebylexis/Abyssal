package graphics;

import org.lwjgl.opengl.GL30;

import core.Texture;

/**
 * OpenGL framebuffer object (FBO) with a color texture attachment and a
 * depth renderbuffer. Used to render the game scene off-screen so it can
 * be displayed as an ImGui image in the game viewport panel.
 */
public class Framebuffer {

	private int fboId = 0;
	private Texture texture = null;
	
	public Framebuffer(int width, int height) {
		fboId = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboId);
		
		texture = new Texture(width, height);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, texture.getId(), 0);
		
		int rboId = GL30.glGenRenderbuffers();
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, rboId);
		GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH_COMPONENT32, width, height);
		GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, rboId);
		
		if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
			assert false : "Error: framebuffer is not complete";
		}
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}
	
	public void bind() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboId);
	}
	
	public void unbind() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}
	
	public int getFboId() {
		return fboId;
	}
	
	public int getTextureId() {
		return texture.getId();
	}
}
