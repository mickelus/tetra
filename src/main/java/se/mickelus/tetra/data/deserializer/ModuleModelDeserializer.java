package se.mickelus.tetra.data.deserializer;

import com.google.gson.*;
import se.mickelus.mutil.data.deserializer.ResourceLocationDeserializer;
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
            data.tint = getTint(jsonObject.get("tint").getAsString());
            data.overlayTint = data.tint;
        }

        if (jsonObject.has("overlayTint")) {
            data.overlayTint = getTint(jsonObject.get("overlayTint").getAsString());
        }

        return data;
    }

    private int getTint(String value) {
        if (ItemColors.exists(value)) {
            return ItemColors.get(value);
        } else {
            try {
                return (int) Long.parseLong(value, 16);
            } catch (NumberFormatException e) {
                throw new JsonParseException("Could not parse tint '" + value + "' when deserializing module model, unknown color or malformed hexadecimal", e);
            }
        }
    }
}
