package core;
import java.util.Arrays;

import org.lwjgl.glfw.GLFW;

/**
 * Singleton that tracks keyboard state via GLFW callbacks. Provides both
 * held-down (isKeyPressed) and single-frame edge-triggered (keyBeginPress)
 * queries. keyBeginPress is cleared at the end of each frame.
 */
public class KeyListener {

	private static KeyListener instance;
	private boolean[] keyPressed = new boolean[350];
	private boolean[] keyBeginPress = new boolean[350];
	
	private KeyListener() {
		
	}
	
	public static void endFrame() {
		Arrays.fill(get().keyBeginPress, false);
	}
	
	public static KeyListener get() {
		if(KeyListener.instance == null) {
			KeyListener.instance = new KeyListener();
		}
		
		return KeyListener.instance;
	}
	
	public static void keyCallback(long window, int key, int scancode, int action, int mods) {
		if(action == GLFW.GLFW_PRESS) {
			get().keyPressed[key] = true;
			get().keyBeginPress[key] = true;
		}else if(action == GLFW.GLFW_RELEASE) {
			get().keyPressed[key] = false;
			get().keyBeginPress[key] = false;
		}
	}
	
	public static boolean isKeyPressed(int keycode) {
		return get().keyPressed[keycode];
	}
	
	public static boolean keyBeginPress(int keycode) {
		return get().keyBeginPress[keycode];
	}
}
