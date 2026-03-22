package se.mickelus.tetra.data.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import se.mickelus.mutil.gui.SimpleColor;
import se.mickelus.tetra.items.modular.ItemColors;

import java.lang.reflect.Type;

public class SimpleColorDeserializer implements JsonDeserializer<SimpleColor> {
    public static SimpleColor deserialize(JsonElement json) {
        try {
            if (json.getAsJsonPrimitive().isNumber()) {
                return new SimpleColor(json.getAsInt());
            }
            if (ItemColors.exists(json.getAsString())) {
                return new SimpleColor(ItemColors.get(json.getAsString()));
            }
            return new SimpleColor((int) Long.parseLong(json.getAsString(), 16));
        } catch (Exception e) {
            throw new JsonParseException("Failed to parse color: " + json, e);
        }
    }

    @Override
    public SimpleColor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return deserialize(json);
    }
}
