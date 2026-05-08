package scenes;
import java.io.File;
import java.util.Collection;

import org.joml.Vector2f;

import components.EditorCamera;
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
import imgui.flag.ImGuiWindowFlags;
import physics2d.components.Box2DCollider;
import physics2d.components.Rigidbody2D;
import physics2d.enums.BodyType;
import util.AssetPool;

/**
 * SceneInitializer for editor mode. Loads all art assets, deserializes the
 * saved level, sets up the EditorCamera, gizmo system, grid lines, and the
 * tile palette ImGui panel used to place objects into the scene.
 */
public class LevelEditorSceneInitializer extends SceneInitializer {
	
	private Spritesheet sprites;
	private GameObject levelEditorStuff;
	
	public LevelEditorSceneInitializer() {
		
	}
	
	@Override
	public void init(Scene scene) {
		sprites = AssetPool.getSpritesheet("res/textures/mainlevbuild.png");
		Spritesheet gizmos = AssetPool.getSpritesheet("res/textures/gizmos.png");
		
		levelEditorStuff = scene.createGameObject("LevelEditor");
		levelEditorStuff.setNoSerialize();
		levelEditorStuff.addComponent(new MouseControls());
		levelEditorStuff.addComponent(new KeyControls());
		levelEditorStuff.addComponent(new GridLines());
		levelEditorStuff.addComponent(new EditorCamera(scene.camera()));
		levelEditorStuff.addComponent(new GizmoSystem(gizmos));
		scene.addGameObjectToScene(levelEditorStuff);
	}
	
	@Override
	public void loadResources(Scene scene) {
		AssetPool.getShader("res/shaders/default.glsl");
		
		AssetPool.addSpritesheet("res/textures/spritesheet.png", new Spritesheet(AssetPool.getTexture("res/textures/spritesheet.png"), 50, 37, 72, 0));
		AssetPool.addSpritesheet("res/textures/mainlevbuild.png", new Spritesheet(AssetPool.getTexture("res/textures/mainlevbuild.png"), 16, 16, 4096, 0));
		AssetPool.addSpritesheet("res/textures/gizmos.png", new Spritesheet(AssetPool.getTexture("res/textures/gizmos.png"), 24, 48, 3, 0));
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
		
		ImGui.begin("level editor stuff");
		levelEditorStuff.imgui();
		ImGui.end();
		
		ImGui.begin("Objects");
		
		if(ImGui.beginTabBar("WindowTabBar")) {
			if(ImGui.beginTabItem("Blocks")) {
				ImVec2 windowPos = new ImVec2();
				ImGui.getWindowPos(windowPos);
				ImVec2 windowSize = new ImVec2();
				ImGui.getWindowSize(windowSize);
				ImVec2 itemSpacing = new ImVec2();
				ImGui.getStyle().getItemSpacing(itemSpacing);
				
				float windowX2 = windowPos.x + windowSize.x;
				for(int i = 0; i < sprites.size(); i++) {
					
					Sprite sprite = sprites.getSprite(i);
					float spriteWidth = sprite.getWidth() * 1.5f;
					float spriteHeight = sprite.getHeight() * 1.5f;
					int id = sprite.getTexId();
					Vector2f[] texCoords = sprite.getTexCoords();
					
					ImGui.pushID(i);
					if(ImGui.imageButton("##sprite", id, spriteWidth, spriteHeight, texCoords[2].x, texCoords[0].y, texCoords[0].x, texCoords[2].y)) {
						GameObject object = Prefabs.generateSpriteObject(sprite, 0.25f, 0.25f);
						Rigidbody2D rb = new Rigidbody2D();
						rb.setBodyType(BodyType.Static);
						object.addComponent(rb);
						Box2DCollider b2d = new Box2DCollider();
						b2d.setHalfSize(new Vector2f(0.25f, 0.25f));
						object.addComponent(b2d);
						object.addComponent(new Ground());
						levelEditorStuff.getComponent(MouseControls.class).pickupObject(object);
					}
					ImGui.popID();
					
					ImVec2 lastButtonPos = new ImVec2();
					ImGui.getItemRectMax(lastButtonPos);
					float lastButtonX2 = lastButtonPos.x;
					float nextButtonX2 = lastButtonX2 + itemSpacing.x + spriteWidth;
					if(i + 1 < sprites.size() && nextButtonX2 < windowX2) {
						ImGui.sameLine();
					}
				}
				
				ImGui.endTabItem();
			}
			
			if(ImGui.beginTabItem("Prefabs")) {
				Spritesheet playerSprites = AssetPool.getSpritesheet("res/textures/spritesheet.png");
				Sprite sprite = playerSprites.getSprite(0);
				float spriteWidth = sprite.getWidth() * 1.5f;
				float spriteHeight = sprite.getHeight() * 1.5f;
				int id = sprite.getTexId();
				Vector2f[] texCoords = sprite.getTexCoords();
				
				if(ImGui.imageButton("##player", id, spriteWidth, spriteHeight, texCoords[2].x, texCoords[0].y, texCoords[0].x, texCoords[2].y)) {
					GameObject object = Prefabs.generatePlayer();
					levelEditorStuff.getComponent(MouseControls.class).pickupObject(object);
				}
				ImGui.sameLine();
				
				ImGui.endTabItem();
			}
			
			if(ImGui.beginTabItem("Sounds")) {
				Collection<Sound> sounds = AssetPool.getAllSounds();
				for(Sound sound : sounds) {
					File tmp = new File(sound.getFilePath());
					if(ImGui.button(tmp.getName())) {
						if(!sound.isPlaying()) {
							sound.play();
						}else {
							sound.stop();
						}
					}
					
					if(ImGui.getContentRegionAvailX() > 100) {
						ImGui.sameLine();
					}
				}
				
				ImGui.endTabItem();
			}
			ImGui.endTabBar();
		}
		
		ImGui.end();
	}
}
