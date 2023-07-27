package se.mickelus.tetra.data.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.ItemTransforms;

import java.lang.reflect.Type;
import java.util.Arrays;

public class TransformTypeDeserializer implements JsonDeserializer<ItemTransforms.TransformType> {
    @Override
    public ItemTransforms.TransformType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String string = json.getAsString();
        try {
            return Arrays.stream(ItemTransforms.TransformType.values())
                    .filter(type -> type.getSerializeName().equals(string.toLowerCase()))
                    .findFirst()
                    .orElseThrow(() -> new JsonParseException("Tried to parse missing or invalid TransformType: " + json));
        } catch (JsonParseException | NullPointerException e) {
            throw new JsonParseException("Tried to parse faulty TransformType: " + json, e);
        }
    }
}
