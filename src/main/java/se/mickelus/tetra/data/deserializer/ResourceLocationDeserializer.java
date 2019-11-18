package se.mickelus.tetra.data.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Type;

public class ResourceLocationDeserializer implements JsonDeserializer<ResourceLocation> {

    public static ResourceLocation deserialize(JsonElement json) throws JsonParseException {
        return new ResourceLocation(json.getAsString());
    }

    @Override
    public ResourceLocation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return deserialize(json);
    }
}