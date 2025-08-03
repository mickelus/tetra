package se.mickelus.tetra.effect.data;

import com.google.gson.*;
import se.mickelus.mutil.util.JsonOptional;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ItemEffectTrigger {
    private static final Map<String, Function<JsonElement, ItemEffectTrigger>> deserializers = new HashMap<>();

    protected final String type;

    public ItemEffectTrigger(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static void registerTrigger(String key, Function<JsonElement, ItemEffectTrigger> deserializer) {
        deserializers.put(key, deserializer);
    }

    public static class Deserializer implements JsonDeserializer<ItemEffectTrigger> {
        @Override
        public ItemEffectTrigger deserialize(JsonElement jsonElement, Type type,
                JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                String key = JsonOptional.field(jsonObject, "type")
                        .map(JsonElement::getAsString)
                        .orElse("tetra:default");
                if (deserializers.containsKey(key)) {
                    return deserializers.get(key).apply(jsonElement);
                }
                return new ItemEffectTrigger(key);
            } else {
                return new ItemEffectTrigger(jsonElement.getAsString());
            }
        }
    }
}
