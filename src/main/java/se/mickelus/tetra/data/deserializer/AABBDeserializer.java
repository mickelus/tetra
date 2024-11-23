package se.mickelus.tetra.data.deserializer;

import com.google.gson.*;
import net.minecraft.world.phys.AABB;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;

@ParametersAreNonnullByDefault
public class AABBDeserializer implements JsonDeserializer<AABB> {
    public static AABB deserialize(JsonElement json) throws JsonParseException {
        JsonArray array = json.getAsJsonArray();
        if (array.size() == 6) {
            return new AABB(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat(), array.get(3).getAsFloat(), array.get(4).getAsFloat(), array.get(5).getAsFloat());
        }
        throw new JsonParseException("Tried to parse faulty AABB (unexpected length): " + json);
    }

    @Override
    public AABB deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return deserialize(json);
    }
}
