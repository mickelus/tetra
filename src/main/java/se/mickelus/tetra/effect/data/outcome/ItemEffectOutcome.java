package se.mickelus.tetra.effect.data.outcome;

import com.google.gson.*;
import se.mickelus.mutil.util.JsonOptional;
import se.mickelus.tetra.effect.data.ItemEffectContext;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class ItemEffectOutcome {
    private static final Map<String, Function<JsonElement, ItemEffectOutcome>> deserializers = new HashMap<>();

    public abstract boolean perform(ItemEffectContext context);

    public static void registerCondition(String key, Function<JsonElement, ItemEffectOutcome> deserializer) {
        deserializers.put(key, deserializer);
    }

    public static class Deserializer implements JsonDeserializer<ItemEffectOutcome> {
        @Override
        public ItemEffectOutcome deserialize(JsonElement jsonElement, Type type,
                JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String key = JsonOptional.field(jsonObject, "type")
                    .map(JsonElement::getAsString)
                    .orElse("tetra:default");
            if (deserializers.containsKey(key)) {
                return deserializers.get(key).apply(jsonElement);
            }
            throw new JsonParseException("No deserializer found for DataEffectOutcome type: " + key);
        }
    }
}
