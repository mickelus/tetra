package se.mickelus.tetra.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.advancements.criterion.ItemPredicate;
import se.mickelus.tetra.blocks.PropertyMatcher;

import java.lang.reflect.Type;

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
