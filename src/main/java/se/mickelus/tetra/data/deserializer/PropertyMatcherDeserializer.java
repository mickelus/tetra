package se.mickelus.tetra.data.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import se.mickelus.tetra.blocks.PropertyMatcher;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;

@ParametersAreNonnullByDefault
public class PropertyMatcherDeserializer implements JsonDeserializer<PropertyMatcher> {
    @Override
    public PropertyMatcher deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return PropertyMatcher.deserialize(json);
        } catch (JsonParseException e) {
            // todo: debug level log
            return null;
        }
    }
}
