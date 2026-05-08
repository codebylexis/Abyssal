package graphics;

import org.joml.Vector2i;
import org.lwjgl.opengl.GL30;

/**
 * GPU-based mouse picking. Renders the scene a second time with each object
 * drawn in a unique color derived from its entity ID (stored in the vertex
 * entity-ID channel). readPixel() and readPixels() read back that color to
 * identify which object(s) the cursor is over without any CPU-side hit testing.
 */
public class PickingTexture {

	private int pickingTextureId;
	private int fbo;
	private int depthTexture;
	
	public PickingTexture(int width, int height) {
		if(!init(width, height)) {
			assert false : "Error initializing picking texture";
		}
	}
	
	public boolean init(int width, int height) {
		fbo = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
		
		pickingTextureId = GL30.glGenTextures();
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, pickingTextureId);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_REPEAT);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_REPEAT);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_NEAREST);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_NEAREST);
		GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGB32F, width, height, 0, GL30.GL_RGB, GL30.GL_FLOAT, 0);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, this.pickingTextureId, 0);
		
		GL30.glReadBuffer(GL30.GL_NONE);
		GL30.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
		
		int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
		if(status != GL30.GL_FRAMEBUFFER_COMPLETE) {
			assert false : "Error: framebuffer is not complete " + status;
			return false;
		}
		
		GL30.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, 0);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		
		return true;
	}
	
	public void enableWriting() {
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, fbo);
	}
	
	public void disableWriting() {
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
	}
	
	public int readPixel(int x, int y) {
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, fbo);
		GL30.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
		
		float[] pixels = new float[3];
		GL30.glReadPixels(x, y, 1, 1, GL30.GL_RGB, GL30.GL_FLOAT, pixels);
		
		return (int)(pixels[0]) - 1;
	}
	
	public float[] readPixels(Vector2i start, Vector2i end) {
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, fbo);
		GL30.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
		
		Vector2i size = new Vector2i(end).sub(start).absolute();
		int numPixels = size.x * size.y;
		float[] pixels = new float[3 * numPixels];
		GL30.glReadPixels(start.x, start.y, size.x, size.y, GL30.GL_RGB, GL30.GL_FLOAT, pixels);
		
		for(int i = 0; i < pixels.length; i++) {
			pixels[i] -= 1;
		}
		
		return pixels;
	}
}
