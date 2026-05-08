package scenes;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.joml.Vector2f;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import components.Component;
import core.Camera;
import core.ComponentDeserializer;
import core.GameObject;
import core.GameObjectDeserializer;
import core.Transform;
import graphics.Renderer;
import physics2d.Physics2D;

/**
 * Container for all game objects in a level. Delegates initialization to a
 * SceneInitializer, then owns the update loop, renderer, physics world, and
 * JSON save/load for the level file.
 */
public class Scene {
	
	private Camera camera;
	private List<GameObject> gameObjects;
	private Renderer renderer = new Renderer();
	private boolean isRunning;
	private Physics2D physics2D;
	
	private SceneInitializer sceneInitializer;

	public Scene(SceneInitializer sceneInitializer) {
		this.sceneInitializer = sceneInitializer;
		physics2D = new Physics2D();
		renderer = new Renderer();
		gameObjects = new ArrayList<GameObject>();
		isRunning = false;
	}
	
	public void init() {
		this.camera = new Camera(new Vector2f());
		sceneInitializer.loadResources(this);
		sceneInitializer.init(this);
	}
	
	public void start() {
		for(int i = 0; i < gameObjects.size(); i++) {
			GameObject go = gameObjects.get(i);
			go.start();
			renderer.add(go);
			physics2D.add(go);
		}
		
		isRunning = true;
	}
	
	public void addGameObjectToScene(GameObject go) {
		if(!isRunning) {
			gameObjects.add(go);
		}else {
			gameObjects.add(go);
			go.start();
			renderer.add(go);
			physics2D.add(go);
		}
	}
	
	public GameObject getGameObject(int id) {
		Optional<GameObject> res = this.gameObjects.stream().filter(gameObject -> gameObject.getUId() == id).findFirst();
		return res.orElse(null);
	}
	
	public List<GameObject> getGameObjects() {
		return gameObjects;
	}
	
	public void imgui() {
		sceneInitializer.imgui();
	}
	
	public GameObject createGameObject(String name) {
		GameObject go = new GameObject(name);
		go.addComponent(new Transform());
		go.transform = go.getComponent(Transform.class);
		return go;
	}
	
	public void editorUpdate(float dt) {
		camera.adjustProjection();
		
		for(int i = 0; i < gameObjects.size(); i++) {
			GameObject go = gameObjects.get(i);
			go.editorUpdate(dt);
			
			if(go.isDead()) {
				gameObjects.remove(i);
				renderer.destroyGameObject(go);
				physics2D.destroyGameObject(go);
				i--;
			}
		}
	}
	
	public void update(float delta) {
		camera.adjustProjection();
		physics2D.update(delta);
		
		for(int i = 0; i < gameObjects.size(); i++) {
			GameObject go = gameObjects.get(i);
			go.update(delta);
			
			if(go.isDead()) {
				gameObjects.remove(i);
				renderer.destroyGameObject(go);
				physics2D.destroyGameObject(go);
				i--;
			}
		}
	}
	
	public void render() {
		renderer.render();
	}
	
	public void destroy() {
		for(GameObject go : gameObjects) {
			go.destroy();
		}
	}
	
	public <T extends Component> GameObject getGameObjectWith(Class<T> clazz) {
		for(GameObject go : gameObjects) {
			if(go.getComponent(clazz) != null) {
				return go;
			}
		}
		
		return null;
	}
	
	public void save() {
		Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Component.class, new ComponentDeserializer()).registerTypeAdapter(GameObject.class, new GameObjectDeserializer()).enableComplexMapKeySerialization().create();
		
		try {
			FileWriter writer = new FileWriter("level.txt");
			List<GameObject> objsToSerialize = new ArrayList<>();
			for(GameObject obj : gameObjects) {
				if(obj.doSerialization()) {
					objsToSerialize.add(obj);
				}
			}
			writer.write(gson.toJson(objsToSerialize));
			writer.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void load() {
		Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Component.class, new ComponentDeserializer()).registerTypeAdapter(GameObject.class, new GameObjectDeserializer()).enableComplexMapKeySerialization().create();
		String inFile = "";
		try {
			inFile = new String(Files.readAllBytes(Paths.get("level.txt")));
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		if(!inFile.equals("")) {
			int maxGoId = -1;
			int maxCompId = -1;
			GameObject[] objs = gson.fromJson(inFile, GameObject[].class);
			for(int i = 0; i < objs.length; i++) {
				addGameObjectToScene(objs[i]);
				
				for(Component c : objs[i].getAllComponents()) {
					if(c.getUId() > maxCompId) {
						maxCompId = c.getUId();
					}
				}
				
				if(objs[i].getUId() > maxGoId) {
					maxGoId = objs[i].getUId();
				}
			}
			
			maxGoId++;
			maxCompId++;
			GameObject.init(maxGoId);
			Component.init(maxCompId); 
		}
	}
	
	public Camera camera() {
		return this.camera;
	}
	
	public Physics2D getPhysics() {
		return physics2D;
	}
}
