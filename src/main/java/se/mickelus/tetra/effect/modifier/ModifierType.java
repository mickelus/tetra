package se.mickelus.tetra.effect.modifier;

import com.google.gson.*;
import se.mickelus.mutil.util.JsonOptional;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ModifierType {
    private static final Map<String, Function<JsonElement, ModifierType>> deserializers = new HashMap<>();

    public final String key;

    public ModifierType(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static void registerType(String key, Function<JsonElement, ModifierType> deserializer) {
        deserializers.put(key, deserializer);
    }

    public static class Deserializer implements JsonDeserializer<ModifierType> {
        @Override
        public ModifierType deserialize(JsonElement jsonElement, Type type,
                JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                String key = JsonOptional.field(jsonObject, "type")
                        .map(JsonElement::getAsString)
                        .orElse("tetra:unknown");
                if (deserializers.containsKey(key)) {
                    return deserializers.get(key).apply(jsonElement);
                }
                return new ModifierType(key);
            } else {
                return new ModifierType(jsonElement.getAsString());
            }
        }
    }
}
