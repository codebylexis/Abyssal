package util;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import components.Spritesheet;
import core.Shader;
import core.Sound;
import core.Texture;

/**
 * Static cache for shared engine assets. Resources are keyed by their absolute
 * file path so the same file is never loaded more than once per session.
 * Spritesheets must be registered with addSpritesheet before getSpritesheet
 * can return them.
 */
public class AssetPool {

	private static Map<String, Shader> shaders = new HashMap<String, Shader>();
	private static Map<String, Texture> textures = new HashMap<String, Texture>();
	private static Map<String, Spritesheet> spritesheets = new HashMap<String, Spritesheet>();
	private static Map<String, Sound> sounds = new HashMap<String, Sound>();
	
	public static Shader getShader(String resourceName) {
		File file = new File(resourceName);
		if(AssetPool.shaders.containsKey(file.getAbsolutePath())) {
			return AssetPool.shaders.get(file.getAbsolutePath());
		}else {
			Shader shader = new Shader(resourceName);
			shader.compile();
			AssetPool.shaders.put(file.getAbsolutePath(), shader);
			return shader;
		}
	}
	
	public static Texture getTexture(String resourceName) {
		File file = new File(resourceName);
		if(AssetPool.textures.containsKey(file.getAbsolutePath())) {
			return AssetPool.textures.get(file.getAbsolutePath());
		}else {
			Texture texture = new Texture();
			texture.init(resourceName);
			AssetPool.textures.put(file.getAbsolutePath(), texture);
			return texture;
		}
	}
	
	public static void addSpritesheet(String resourceName, Spritesheet spritesheet) {
		File file = new File(resourceName);
		if(!AssetPool.spritesheets.containsKey(file.getAbsolutePath())) {
			AssetPool.spritesheets.put(file.getAbsolutePath(), spritesheet);
		}
	}
	
	public static Spritesheet getSpritesheet(String resourceName) {
		File file = new File(resourceName);
		if(!AssetPool.spritesheets.containsKey(file.getAbsolutePath())) {
			assert false : "Error: tried to access spritesheet " + resourceName;
		}
		
		return AssetPool.spritesheets.getOrDefault(file.getAbsolutePath(), null);
	}
	
	public static Collection<Sound> getAllSounds() {
		return sounds.values();
	}
	
	public static Sound getSound(String soundFile) {
		File file = new File(soundFile);
		if(sounds.containsKey(file.getAbsolutePath())) {
			return sounds.get(file.getAbsolutePath());
		}else {
			assert false : "Sound file not added " + soundFile;
		}
		
		return null;
	}
	
	public static Sound addSound(String soundFile, boolean loops) {
		File file = new File(soundFile);
		if(sounds.containsKey(file.getAbsolutePath())) {
			return sounds.get(file.getAbsolutePath());
		}else {
			Sound sound = new Sound(file.getAbsolutePath(), loops);
			AssetPool.sounds.put(file.getAbsolutePath(), sound);
			return sound;
		}
	}
}
