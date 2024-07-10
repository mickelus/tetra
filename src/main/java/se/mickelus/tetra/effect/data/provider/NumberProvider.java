package se.mickelus.tetra.effect.data.provider;

import com.google.gson.*;
import se.mickelus.mutil.util.JsonOptional;
import se.mickelus.tetra.effect.data.ItemEffectContext;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public interface NumberProvider {
    float getValue(ItemEffectContext context);

    default int getIntegerValue(ItemEffectContext context) {
        return Math.round(getValue(context));
    }

    static void register(String key, Function<JsonElement, NumberProvider> deserializer) {
        Deserializer.deserializers.put(key, deserializer);
    }

    class Deserializer implements JsonDeserializer<NumberProvider> {
        static final Map<String, Function<JsonElement, NumberProvider>> deserializers = new HashMap<>();

        @Override
        public NumberProvider deserialize(JsonElement jsonElement, Type type,
                JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (jsonElement.isJsonPrimitive()) {
                JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
                if (primitive.isString()) {
                    return ExpressionNumberProvider.parseExpression(primitive.getAsString());
                } else if (primitive.isNumber()) {
                    return new FixedNumberProvider(primitive.getAsFloat());
                }
            }
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String key = JsonOptional.field(jsonObject, "type")
                    .map(JsonElement::getAsString)
                    .orElse(null);

            if (deserializers.containsKey(key)) {
                return deserializers.get(key).apply(jsonElement);
            }
            throw new JsonParseException("No deserializer found for NumberProvider type: " + key);
        }
    }
}
