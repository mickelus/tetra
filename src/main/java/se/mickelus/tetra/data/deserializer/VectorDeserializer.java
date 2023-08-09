package se.mickelus.tetra.data.deserializer;

import com.google.gson.*;
import org.joml.Vector3f;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;

@ParametersAreNonnullByDefault
public class VectorDeserializer implements JsonDeserializer<Vector3f> {
    public static Vector3f deserialize(JsonElement json) throws JsonParseException {
        JsonArray array = json.getAsJsonArray();
        if (array.size() == 3) {
            return new Vector3f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
        }
        throw new JsonParseException("Tried to parse faulty Vector3f: " + json);
    }

    @Override
    public Vector3f deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return deserialize(json);
    }
}
