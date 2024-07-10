package se.mickelus.tetra.effect.data.provider;

import com.google.gson.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import se.mickelus.mutil.util.JsonOptional;
import se.mickelus.tetra.effect.data.ItemEffectContext;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public interface PositionProvider {
    Vec3 getPosition(ItemEffectContext context);

    default BlockPos getBlockPos(ItemEffectContext context) {
        Vec3 pos = getPosition(context);
        return new BlockPos((int) pos.x, (int) pos.y, (int) pos.z);
    }

    static void register(String key, Function<JsonObject, PositionProvider> deserializer) {
        PositionProvider.Deserializer.deserializers.put(key, deserializer);
    }

    class Deserializer implements JsonDeserializer<PositionProvider> {
        static final Map<String, Function<JsonObject, PositionProvider>> deserializers = new HashMap<>();

        @Override
        public PositionProvider deserialize(JsonElement jsonElement, Type type,
                JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (jsonElement.isJsonPrimitive()) {
                // get default
            }
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String key = JsonOptional.field(jsonObject, "type")
                    .map(JsonElement::getAsString)
                    .orElse(null);

            if (deserializers.containsKey(key)) {
                return deserializers.get(key).apply(jsonObject);
            }
            throw new JsonParseException("No deserializer found for PositionProvider type: " + key);
        }
    }
}
