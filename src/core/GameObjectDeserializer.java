package core;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import components.Component;

/**
 * GSON deserializer for GameObject. Reconstructs the object's name and
 * component list, then wires up the Transform reference so callers can use
 * go.transform immediately after deserialization.
 */
public class GameObjectDeserializer implements JsonDeserializer<GameObject> {

	@Override
	public GameObject deserialize(JsonElement json, Type typeOf, JsonDeserializationContext context) throws JsonParseException {
		JsonObject jsonObject = json.getAsJsonObject();
		String name = jsonObject.get("name").getAsString();
		JsonArray components = jsonObject.getAsJsonArray("components");
		
		GameObject go = new GameObject(name);
		for(JsonElement e : components) {
			Component c = context.deserialize(e, Component.class);
			go.addComponent(c);
		}
		go.transform = go.getComponent(Transform.class);
		
		return go;
	}
}
