package se.mickelus.tetra.gui.stats.data;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import se.mickelus.mutil.util.JsonOptional;
import se.mickelus.tetra.gui.stats.getter.IStatFormat;
import se.mickelus.tetra.gui.stats.getter.StatFormat;
import se.mickelus.tetra.gui.stats.getter.StatFormatRoman;

import java.util.Map;

public class StatFormatDeserializers {

    static final Map<String, String> predefinedFormats = ImmutableMap.<String, String>builder()
            .put("integer", "%.0f")
            .put("integer_signed", "%+.0f")
            .put("single_decimal", "%.01f")
            .put("single_decimal_signed", "%+.01f")
            .put("double_decimal", "%.02f")
            .put("double_decimal_signed", "%+.02f")
            .put("percentage", "%.0f%%")
            .put("percentage_signed", "%+.0f%%")
            .put("percentage_decimal", "%.01f%%")
            .put("percentage_decimal_signed", "%+.01f%%")
            .build();
    static final Map<String, String> predefinedDiffFormats = ImmutableMap.<String, String>builder()
            .put("integer", "%+.0f")
            .put("single_decimal", "%+.01f")
            .put("double_decimal", "%+.02f")
            .put("percentage", "%+.0f%%")
            .put("percentage_decimal", "%+.01f%%")
            .build();

    public static IStatFormat basicStatformat(JsonElement json) {
        if (!json.isJsonObject()) {
            throw new JsonParseException("Expected object, got " + json.getClass().getSimpleName());
        }
        JsonObject jsonObject = json.getAsJsonObject();
        String format = JsonOptional.field(jsonObject, "format")
                .map(JsonElement::getAsString)
                .map(val -> predefinedFormats.getOrDefault(val, val))
                .orElseThrow(() -> new JsonParseException("Missing required field 'format'"));

        return new StatFormat(format);
    }

    public static IStatFormat abbreviateStatformat(JsonElement json) {
        return StatFormat.abbreviate;
    }

    public static IStatFormat romanStatformat(JsonElement json) {
        return StatFormatRoman.instance;
    }
}
