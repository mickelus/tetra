package se.mickelus.tetra.data.deserializer;

import com.google.gson.*;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;

@ParametersAreNonnullByDefault
public class QuaternionDeserializer implements JsonDeserializer<Quaternion> {
    public static Quaternion deserialize(JsonElement json) throws JsonParseException {
        JsonArray array = json.getAsJsonArray();
        if (array.size() == 3) {
            return Quaternion.fromXYZDegrees(new Vector3f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat()));
        }
        if (array.size() == 4) {
            return new Quaternion(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat(), array.get(3).getAsFloat());
        }
        throw new JsonParseException("Tried to parse faulty Quaternion: " + json);
    }

    @Override
    public Quaternion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return deserialize(json);
    }
}
