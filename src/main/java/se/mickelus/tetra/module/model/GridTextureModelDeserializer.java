package se.mickelus.tetra.module.model;

import com.google.gson.*;
import com.mojang.math.Transformation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemDisplayContext;
import se.mickelus.mutil.data.deserializer.ResourceLocationDeserializer;
import se.mickelus.tetra.data.deserializer.SimpleColorDeserializer;
import se.mickelus.tetra.module.Priority;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;

@ParametersAreNonnullByDefault
public class GridTextureModelDeserializer implements JsonDeserializer<GridTextureModelData> {

    @Override
    public GridTextureModelData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        return new GridTextureModelData(
                jsonObject.has("type") ? ResourceLocationDeserializer.deserialize(jsonObject.get("type")) : GridTextureModelData.TYPE,
                new ResourceLocation(GsonHelper.getAsString(jsonObject, "location")),
                jsonObject.has("renderType") ? ResourceLocationDeserializer.deserialize(jsonObject.get("renderType")) : null,
                jsonObject.has("transform") ? context.deserialize(jsonObject.getAsJsonObject("transform"), Transformation.class) : null,
                jsonObject.has("emission") ? jsonObject.get("emission").getAsInt() : null,
                jsonObject.has("tint") ? SimpleColorDeserializer.deserialize(jsonObject.get("tint")) : null,
                jsonObject.has("overlayTint") ? SimpleColorDeserializer.deserialize(jsonObject.get("overlayTint")) : null,
                jsonObject.has("renderLayer") ? context.deserialize(jsonObject.get("renderLayer"), Priority.class) : null,
                jsonObject.has("invertPerspectives") ? context.deserialize(jsonObject.get("invertPerspectives"), Boolean.class) : null,
                jsonObject.has("perspectives") ? context.deserialize(jsonObject.get("perspectives"), ItemDisplayContext[].class) : null
        );
    }
}
