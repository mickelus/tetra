package se.mickelus.tetra.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
@ParametersAreNonnullByDefault
public class JsonOptional {
    public static  Optional<JsonElement> field(JsonObject object, String field) {
        if (object.has(field)) {
            return Optional.of(object.get(field));
        }

        return Optional.empty();
    }
}
