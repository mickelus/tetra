package se.mickelus.tetra.effect.data.provider.vector;

import com.google.gson.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import se.mickelus.mutil.util.JsonOptional;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.number.NumberProvider;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public interface VectorProvider {
    Vec3 getVector(ItemEffectContext context);

    default BlockPos getBlockPos(ItemEffectContext context) {
        Vec3 pos = getVector(context);
        return new BlockPos((int) pos.x, (int) pos.y, (int) pos.z);
    }

    static void register(String identifier, Function<JsonObject, VectorProvider> deserializer) {
        Deserializer.deserializers.put(identifier, deserializer);
    }

    static void register(String identifier, Class<? extends VectorProvider> clazz) {
        Deserializer.deserializers.put(identifier, json -> DataManager.gson.fromJson(json, clazz));
    }

    class Deserializer implements JsonDeserializer<VectorProvider> {
        static final Map<String, Function<JsonObject, VectorProvider>> deserializers = new HashMap<>();

        @Override
        public VectorProvider deserialize(JsonElement jsonElement, Type type,
                JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (jsonElement.isJsonPrimitive()) {
                return new ContextVectorProvider(jsonElement.getAsString());
            }
            if (jsonElement.isJsonArray()) {
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                if (jsonArray.size() == 3) {
                    return new NumberVectorProvider(
                            DataManager.gson.fromJson(jsonArray.get(0), NumberProvider.class),
                            DataManager.gson.fromJson(jsonArray.get(1), NumberProvider.class),
                            DataManager.gson.fromJson(jsonArray.get(2), NumberProvider.class));
                }
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
