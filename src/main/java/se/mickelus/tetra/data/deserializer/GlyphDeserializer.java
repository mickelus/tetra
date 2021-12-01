package se.mickelus.tetra.data.deserializer;

import com.google.gson.*;
import se.mickelus.tetra.items.modular.ItemColors;
import se.mickelus.tetra.module.data.GlyphData;

import java.lang.reflect.Type;

public class GlyphDeserializer implements JsonDeserializer<GlyphData> {

    @Override
    public GlyphData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        GlyphData data = new GlyphData();

        if (jsonObject.has("textureLocation")) {
            data.textureLocation = ResourceLocationDeserializer.deserialize(jsonObject.get("textureLocation"));
        }

        if (jsonObject.has("textureX")) {
            data.textureX = jsonObject.get("textureX").getAsInt();
        }

        if (jsonObject.has("textureY")) {
            data.textureY = jsonObject.get("textureY").getAsInt();
        }

        if (jsonObject.has("tint")) {
            String tint = jsonObject.get("tint").getAsString();
            if (ItemColors.exists(tint)) {
                data.tint = ItemColors.get(tint);
            } else {
                data.tint = (int) Long.parseLong(tint, 16);
            }
        }

        return data;
    }
}
