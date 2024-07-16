package se.mickelus.tetra.effect.data.provider.entity;

import com.google.gson.*;
import net.minecraft.world.entity.Entity;
import se.mickelus.mutil.util.JsonOptional;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.data.ItemEffectContext;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public interface EntityProvider {
    Entity getEntity(ItemEffectContext context);

    static void register(String key, Function<JsonObject, EntityProvider> deserializer) {
        Deserializer.deserializers.put(key, deserializer);
    }

    static void register(String identifier, Class<? extends EntityProvider> clazz) {
        Deserializer.deserializers.put(identifier, json -> DataManager.gson.fromJson(json, clazz));
    }

    class Deserializer implements JsonDeserializer<EntityProvider> {
        static final Map<String, Function<JsonObject, EntityProvider>> deserializers = new HashMap<>();

        @Override
        public EntityProvider deserialize(JsonElement jsonElement, Type type,
                JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (jsonElement.isJsonPrimitive()) {
                return new StandardEntityProvider(StandardEntityProvider.Target.valueOf(jsonElement.getAsString()));
            }
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String key = JsonOptional.field(jsonObject, "type")
                    .map(JsonElement::getAsString)
                    .orElse(null);

            if (deserializers.containsKey(key)) {
                return deserializers.get(key).apply(jsonObject);
            }
            throw new JsonParseException("No deserializer found for EntityProvider type: " + key);
        }
    }
}
