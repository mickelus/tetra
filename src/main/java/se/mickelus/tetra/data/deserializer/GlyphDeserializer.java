package se.mickelus.tetra.data.deserializer;

import java.lang.reflect.Type;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import se.mickelus.tetra.module.data.GlyphData;

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
            data.tint = (int) Long.parseLong(jsonObject.get("tint").getAsString(), 16);
        }

        return data;
    }
}
