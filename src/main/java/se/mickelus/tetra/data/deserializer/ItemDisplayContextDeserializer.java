package se.mickelus.tetra.data.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.world.item.ItemDisplayContext;

import java.lang.reflect.Type;
import java.util.Arrays;

public class ItemDisplayContextDeserializer implements JsonDeserializer<ItemDisplayContext> {
    @Override
    public ItemDisplayContext deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String string = json.getAsString();
        try {
            return Arrays.stream(ItemDisplayContext.values())
                    .filter(type -> type.getSerializedName().equals(string.toLowerCase()))
                    .findFirst()
                    .orElseThrow(() -> new JsonParseException("Tried to parse missing or invalid TransformType: " + json));
        } catch (JsonParseException | NullPointerException e) {
            throw new JsonParseException("Tried to parse faulty TransformType: " + json, e);
        }
    }
}
