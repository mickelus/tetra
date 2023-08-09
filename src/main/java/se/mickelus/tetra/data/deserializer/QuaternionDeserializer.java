package se.mickelus.tetra.data.deserializer;

import com.google.gson.*;
import org.joml.Quaternionf;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;

@ParametersAreNonnullByDefault
public class QuaternionDeserializer implements JsonDeserializer<Quaternionf> {
    public static Quaternionf deserialize(JsonElement json) throws JsonParseException {
        JsonArray array = json.getAsJsonArray();
        if (array.size() == 3) {
            return new Quaternionf().setAngleAxis(0f, array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
        }
        if (array.size() == 4) {
            return new Quaternionf(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat(), array.get(3).getAsFloat());
        }
        throw new JsonParseException("Tried to parse faulty Quaternion: " + json);
    }

    @Override
    public Quaternionf deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return deserialize(json);
    }
}
