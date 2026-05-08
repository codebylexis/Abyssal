package scenes;

import java.io.File;
import java.util.Collection;

import org.joml.Vector2f;

import components.EditorCamera;
import components.GameCamera;
import components.GizmoSystem;
import components.GridLines;
import components.Ground;
import components.KeyControls;
import components.MouseControls;
import components.Sprite;
import components.SpriteRenderer;
import components.Spritesheet;
import components.StateMachine;
import core.GameObject;
import core.Prefabs;
import core.Sound;
import imgui.ImGui;
import imgui.ImVec2;
import physics2d.components.Box2DCollider;
import physics2d.components.Rigidbody2D;
import physics2d.enums.BodyType;
import util.AssetPool;

/**
 * SceneInitializer for gameplay mode. Loads level assets and the saved level
 * file, then spawns a GameCamera that follows the player.
 */
public class LevelSceneInitializer extends SceneInitializer {
	
	public LevelSceneInitializer() {
		
	}
	
	@Override
	public void init(Scene scene) {
		Spritesheet sprites = AssetPool.getSpritesheet("res/textures/mainlevbuild.png");
		
		GameObject cameraObject = scene.createGameObject("GameCamera");
		cameraObject.addComponent(new GameCamera(scene.camera()));
		cameraObject.start();
		scene.addGameObjectToScene(cameraObject);
	}
	
	@Override
	public void loadResources(Scene scene) {
		AssetPool.getShader("res/shaders/default.glsl");
		
		AssetPool.addSpritesheet("res/textures/spritesheet.png", new Spritesheet(AssetPool.getTexture("res/textures/spritesheet.png"), 50, 37, 72, 0));
		AssetPool.addSpritesheet("res/textures/mainlevbuild.png", new Spritesheet(AssetPool.getTexture("res/textures/mainlevbuild.png"), 16, 16, 4096, 0));
		AssetPool.getTexture("res/textures/blendImage2.png");
		
		AssetPool.addSound("res/sounds/coin_sound.ogg", false);
		AssetPool.addSound("res/sounds/main_theme_overworld.ogg", false);
		AssetPool.addSound("res/sounds/path_to_argatha.ogg", false);
		
		for(GameObject g : scene.getGameObjects()) {
			if(g.getComponent(SpriteRenderer.class) != null) {
				SpriteRenderer spr = g.getComponent(SpriteRenderer.class);
				if(spr.getTexture() != null) {
					spr.setTexture(AssetPool.getTexture(spr.getTexture().getFilepath()));
				}
			}
			
			if(g.getComponent(StateMachine.class) != null) {
				StateMachine stateMachine = g.getComponent(StateMachine.class);
				stateMachine.refreshTextures();
			}
		}
	}
	
	@Override
	public void imgui() {
		
	}
}
