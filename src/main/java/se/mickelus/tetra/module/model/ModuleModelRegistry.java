package se.mickelus.tetra.module.model;

import com.google.gson.*;
import se.mickelus.mutil.util.JsonOptional;
import se.mickelus.tetra.data.DataManager;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@ParametersAreNonnullByDefault
public class ModuleModelRegistry {
    private static final Map<String, Function<JsonElement, IModuleModel>> deserializers = new HashMap<>();

    public static void register(String identifier, Function<JsonElement, IModuleModel> deserializer) {
        deserializers.put(identifier, deserializer);
    }

    public static void register(String identifier, Class<? extends IModuleModel> clazz) {
        deserializers.put(identifier, json -> DataManager.gson.fromJson(json, clazz));
    }

    public static class Deserializer implements JsonDeserializer<IModuleModel> {
        @Override
        public IModuleModel deserialize(JsonElement jsonElement, Type type,
                JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String key = JsonOptional.field(jsonObject, "type")
                    .map(JsonElement::getAsString)
                    .orElse("tetra:grid_texture");
            if (deserializers.containsKey(key)) {
                return deserializers.get(key).apply(jsonElement);
            }
            throw new JsonParseException("No deserializer found for module model type: " + key);
        }
    }
}
