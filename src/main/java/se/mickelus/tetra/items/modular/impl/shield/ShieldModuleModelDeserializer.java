package se.mickelus.tetra.items.modular.impl.shield;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import se.mickelus.mutil.gui.SimpleColor;
import se.mickelus.tetra.data.deserializer.SimpleColorDeserializer;
import se.mickelus.tetra.module.Priority;

import java.lang.reflect.Type;

public class ShieldModuleModelDeserializer implements JsonDeserializer<ShieldModuleModel> {

    @Override
    public ShieldModuleModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        return new ShieldModuleModel(
                new ResourceLocation(GsonHelper.getAsString(jsonObject, "type")),
                new ResourceLocation(GsonHelper.getAsString(jsonObject, "model")),
                new ResourceLocation(GsonHelper.getAsString(jsonObject, "texture")),
                jsonObject.has("tint") ? SimpleColorDeserializer.deserialize(jsonObject.get("tint")) : new SimpleColor(0xffffffff),
                jsonObject.has("overlayTint") ? SimpleColorDeserializer.deserialize(jsonObject.get("overlayTint")) : new SimpleColor(0xffffffff),
                jsonObject.has("renderLayer") ? context.deserialize(jsonObject.get("renderLayer"), Priority.class) : null
        );
    }
}
