package se.mickelus.tetra.data.deserializer;

import com.google.gson.*;
import se.mickelus.tetra.items.modular.ItemColors;
import se.mickelus.tetra.module.data.ModuleModel;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;

@ParametersAreNonnullByDefault
public class ModuleModelDeserializer implements JsonDeserializer<ModuleModel> {

    @Override
    public ModuleModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        ModuleModel data = new ModuleModel();

        if (jsonObject.has("type")) {
            data.type = jsonObject.get("type").getAsString();
        }

        if (jsonObject.has("location")) {
            data.location = ResourceLocationDeserializer.deserialize(jsonObject.get("location"));
        }

        if (jsonObject.has("tint")) {
            String tint = jsonObject.get("tint").getAsString();
            if (ItemColors.exists(tint)) {
                data.tint = ItemColors.get(tint);
            } else {
                try {
                    data.tint = (int) Long.parseLong(tint, 16);
                } catch (NumberFormatException e) {
                    throw new JsonParseException("Could not parse tint '" + tint + "' when deserializing module model, unknown color or malformed hexadecimal", e);
                }
            }
        }

        return data;
    }
}
