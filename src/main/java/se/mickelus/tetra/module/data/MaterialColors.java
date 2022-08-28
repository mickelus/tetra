package se.mickelus.tetra.module.data;

import com.google.gson.*;
import net.minecraft.util.Mth;
import se.mickelus.tetra.items.modular.ItemColors;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;

@ParametersAreNonnullByDefault
public class MaterialColors {
    public int texture = 0xffffffff;
    public int glyph = 0xffffffff;
    public int emission = 0;

    public static class Deserializer implements JsonDeserializer<MaterialColors> {

        @Override
        public MaterialColors deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            MaterialColors data = new MaterialColors();

            if (jsonObject.has("texture")) {
                String tint = jsonObject.get("texture").getAsString();
                if (ItemColors.exists(tint)) {
                    data.texture = ItemColors.get(tint);
                } else {
                    data.texture = (int) Long.parseLong(tint, 16);
                }
            }

            if (jsonObject.has("glyph")) {
                String tint = jsonObject.get("glyph").getAsString();
                if (ItemColors.exists(tint)) {
                    data.glyph = ItemColors.get(tint);
                } else {
                    data.glyph = (int) Long.parseLong(tint, 16);
                }
            }

            if (jsonObject.has("emission")) {
                data.emission = Mth.clamp(0, jsonObject.get("emission").getAsInt(), 15);
            }

            return data;
        }
    }
}
