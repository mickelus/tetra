package se.mickelus.tetra.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Optional;

public class JsonOptional {
    public static  Optional<JsonElement> field(JsonObject object, String field) {
        if (object.has(field)) {
            return Optional.of(object.get(field));
        }

        return Optional.empty();
    }
}
