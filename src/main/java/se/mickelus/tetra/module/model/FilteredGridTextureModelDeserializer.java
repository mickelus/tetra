package se.mickelus.tetra.module.model;

import com.google.gson.*;
import com.mojang.math.Transformation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import se.mickelus.mutil.data.deserializer.ResourceLocationDeserializer;
import se.mickelus.tetra.items.modular.ItemColors;
import se.mickelus.tetra.module.Priority;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;

@ParametersAreNonnullByDefault
public class FilteredGridTextureModelDeserializer implements JsonDeserializer<FilteredGridTextureModel> {

    @Override
    public FilteredGridTextureModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        return new FilteredGridTextureModel(
                new ResourceLocation(GsonHelper.getAsString(jsonObject, "location")),
                jsonObject.has("renderType") ? ResourceLocationDeserializer.deserialize(jsonObject.get("renderType")) : null,
                jsonObject.has("transform") ? context.deserialize(jsonObject.getAsJsonObject("transform"), Transformation.class) : null,
                jsonObject.has("emission") ? Mth.clamp(0, jsonObject.get("emission").getAsInt(), 15) : null,
                jsonObject.has("tint") ? getTint(jsonObject.get("tint").getAsString()) : null,
                jsonObject.has("overlayTint") ? getTint(jsonObject.get("overlayTint").getAsString()) : null,
                jsonObject.has("renderLayer") ? context.deserialize(jsonObject.get("renderLayer"), Priority.class) : null,
                jsonObject.has("invertPerspectives") ? context.deserialize(jsonObject.get("invertPerspectives"), Boolean.class) : null,
                jsonObject.has("perspectives") ? context.deserialize(jsonObject.get("perspectives"), ItemDisplayContext[].class) : null,
                GsonHelper.getAsString(jsonObject, "filter")
        );
    }

    private int getTint(String value) {
        if (ItemColors.exists(value)) {
            return ItemColors.get(value);
        } else {
            try {
                return (int) Long.parseLong(value, 16);
            } catch (NumberFormatException e) {
                throw new JsonParseException("Could not parse tint '" + value + "' when deserializing module model, unknown color or malformed " +
                        "hexadecimal", e);
            }
        }
    }
}
