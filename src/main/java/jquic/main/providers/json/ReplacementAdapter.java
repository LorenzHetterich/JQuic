package jquic.main.providers.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class ReplacementAdapter implements JsonDeserializer<Replacement>{

	/**
	 * Deserialize json to replacement
	 * @param json element
	 * @param typeOfT type of elem
	 * @param context for Deserialization
	 * @return Replacement
	 * @throws JsonParseException if error
	 */
	@Override
	public Replacement deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		
		JsonObject obj = json.getAsJsonObject();
		
		ReplacementType type = ReplacementType.valueOf(obj.get("type").getAsString());
		
		if(type.named) {
			NamedReplacement repl = new NamedReplacement(type);
			if(obj.has("match_name"))
				repl.match_name = obj.get("match_name").getAsString();
			if(obj.has("match_val"))
				repl.match_val = obj.get("match_val").getAsString();
			if(obj.has("replace_name"))
				repl.replace_name = obj.get("replace_name").getAsString();
			if(obj.has("replace_val"))
				repl.replace_val = obj.get("replace_val").getAsString();
			
			return repl;
		} else {
			UnnamedReplacement repl = new UnnamedReplacement(type);

			if(obj.has("match"))
				repl.match = obj.get("match").getAsString();
			if(obj.has("replace"))
				repl.replace = obj.get("replace").getAsString();
			
			return repl;
		}
		
	}


}
