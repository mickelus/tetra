package se.mickelus.tetra.gui.stats.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import se.mickelus.mutil.util.JsonOptional;
import se.mickelus.tetra.gui.stats.getter.ILabelGetter;
import se.mickelus.tetra.gui.stats.getter.LabelGetterBasic;

public class LabelGetterDeserializers {

    public static ILabelGetter basicLabelGetter(JsonElement json) {
        if (!json.isJsonObject()) {
            throw new JsonParseException("Expected object, got " + json.getClass().getSimpleName());
        }
        JsonObject jsonObject = json.getAsJsonObject();
        String format = JsonOptional.field(jsonObject, "format")
                .map(JsonElement::getAsString)
                .map(val -> StatFormatDeserializers.predefinedFormats.getOrDefault(val, val))
                .orElseThrow(() -> new JsonParseException("Missing required field 'format'"));
        String diffFormat = JsonOptional.field(jsonObject, "diff_format")
                .map(JsonElement::getAsString)
                .map(val -> StatFormatDeserializers.predefinedDiffFormats.getOrDefault(val, val))
                .orElseThrow(() -> new JsonParseException("Missing required field 'diff_format'"));
        boolean inverted = JsonOptional.field(jsonObject, "inverted")
                .map(JsonElement::getAsBoolean)
                .orElse(false);

        return new LabelGetterBasic(format, diffFormat, inverted);
    }

    public static ILabelGetter noLabelGetter(JsonElement json) {
        return LabelGetterBasic.noLabel;
    }
}
