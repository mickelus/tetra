package se.mickelus.tetra.effect.data.condition;

import com.google.gson.*;
import se.mickelus.mutil.util.JsonOptional;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.data.ItemEffectContext;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class ItemEffectCondition {
    private static final Map<String, Function<JsonElement, ItemEffectCondition>> deserializers = new HashMap<>();


    public abstract boolean test(ItemEffectContext context);

    public static void register(String identifier, Function<JsonElement, ItemEffectCondition> deserializer) {
        deserializers.put(identifier, deserializer);
    }

    public static void register(String identifier, Class<? extends ItemEffectCondition> clazz) {
        deserializers.put(identifier, json -> DataManager.gson.fromJson(json, clazz));
    }

    public static class Deserializer implements JsonDeserializer<ItemEffectCondition> {
        @Override
        public ItemEffectCondition deserialize(JsonElement jsonElement, Type type,
                JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (jsonElement.isJsonPrimitive()) {
                return new FixedItemEffectCondition(jsonElement.getAsBoolean());
            }
            
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String key = JsonOptional.field(jsonObject, "type")
                    .map(JsonElement::getAsString)
                    .orElse("tetra:default");
            if (deserializers.containsKey(key)) {
                return deserializers.get(key).apply(jsonElement);
            }
            throw new JsonParseException("No deserializer found for DataEffectCondition type: " + key);
        }
    }
}
