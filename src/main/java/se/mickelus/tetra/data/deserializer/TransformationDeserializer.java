package se.mickelus.tetra.data.deserializer;

import com.google.gson.*;
import com.mojang.math.Transformation;
import se.mickelus.mutil.util.JsonOptional;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;

@ParametersAreNonnullByDefault
public class TransformationDeserializer implements JsonDeserializer<Transformation> {
    @Override
    public Transformation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        try {
            return new Transformation(
                    JsonOptional.field(object, "translation")
                            .map(VectorDeserializer::deserialize)
                            .orElse(null),
                    JsonOptional.field(object, "rotation")
                            .map(QuaternionDeserializer::deserialize)
                            .orElse(null),
                    JsonOptional.field(object, "scale")
                            .map(VectorDeserializer::deserialize)
                            .orElse(null),
                    JsonOptional.field(object, "rotation")
                            .map(QuaternionDeserializer::deserialize)
                            .orElse(null)
            );
        } catch (JsonParseException e) {
            throw new JsonParseException("Tried to parse faulty Transformation: " + json, e);
        }
    }
}
