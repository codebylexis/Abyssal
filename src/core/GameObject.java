package core;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import components.Component;
import components.SpriteRenderer;
import imgui.ImGui;
import util.AssetPool;
import editor.AbyssImGui;

/**
 * Core entity in the component-object model. Holds a list of Components and
 * forwards lifecycle calls (start, update, editorUpdate, destroy) to each.
 * Every object has a globally unique ID used for picking and serialization.
 */
public class GameObject {

	private static int ID_COUNTER = 0;
	private int uid = -1;
	
	public String name;
	private List<Component> components;
	private boolean doSerialization = true;
	private boolean isDead = false;
	public transient Transform transform;
	
	public GameObject(String name) {
		this.name = name;
		this.components = new ArrayList<Component>();
		
		this.uid = ID_COUNTER++;
	}
	
	public <T extends Component> T getComponent(Class<T> componentClass) {
		for(Component c : components) {
			if(componentClass.isAssignableFrom(c.getClass())) {
				try {
					return componentClass.cast(c);
				}catch(ClassCastException e) {
					e.printStackTrace();
					assert false : "Error: casting component";
				}
			}
		}
		
		return null;
	}
	
	public <T extends Component> void removeComponent(Class<T> componentClass) {
		for(int i = 0; i < components.size(); i++) {
			Component c = components.get(i);
			if(componentClass.isAssignableFrom(c.getClass())) {
				components.remove(i);
				return;
			}
		}
	}
	
	public void addComponent(Component c) {
		c.generateId();
		components.add(c);
		c.gameObject = this;
	}
	
	public void editorUpdate(float dt) {
		for(int i = 0; i < components.size(); i++) {
			components.get(i).editorUpdate(dt);
		}
	}
	
	public void update(float delta) {
		for(int i = 0; i < components.size(); i++) {
			components.get(i).update(delta);
		}
	}
	
	public void start() {
		for(int i = 0; i < components.size(); i++) {
			components.get(i).start();
		}
	}
	
	public void imgui() {
		name = AbyssImGui.inputText("Name: ", name);
		
		for(Component c : components) {
			if(ImGui.collapsingHeader(c.getClass().getSimpleName())) {
				c.imgui();
			}
		}
	}
	
	public void destroy() {
		isDead = true;
		for(int i = 0; i < components.size(); i++) {
			components.get(i).destroy();
		}
	}
	
	public boolean isDead() {
		return isDead;
	}
	
	public int getUId() {
		return this.uid;
	}
	
	public List<Component> getAllComponents() {
		return this.components;
	}
	
	public void setNoSerialize() {
		doSerialization = false;
	}
	
	public boolean doSerialization() {
		return doSerialization;
	}
	
	public GameObject copy() {
		Gson gson = new GsonBuilder().registerTypeAdapter(Component.class, new ComponentDeserializer()).registerTypeAdapter(GameObject.class, new GameObjectDeserializer()).enableComplexMapKeySerialization().create();
		String objAsJson = gson.toJson(this);
		GameObject obj = gson.fromJson(objAsJson, GameObject.class);
		
		obj.generateUId();
		for(Component c : obj.getAllComponents()) {
			c.generateId();
		}
		
		SpriteRenderer sprite = obj.getComponent(SpriteRenderer.class);
		if(sprite != null && sprite.getTexture() != null) {
			sprite.setTexture(AssetPool.getTexture(sprite.getTexture().getFilepath()));
		}
		
		return obj;
	}
	
	public void generateUId() {
		uid = ID_COUNTER++;
	}
	
	public static void init(int maxId) {
		ID_COUNTER = maxId;
	}
}
