package se.mickelus.tetra.module;

import com.google.gson.*;

import java.lang.reflect.Type;

public enum Priority {
    LOWEST,
    LOWER,
    LOW,
    BASE,
    HIGH,
    HIGHER,
    HIGHEST;

    public static class PriorityAdapter implements JsonDeserializer<Priority> {

        @Override
        public Priority deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                return Priority.valueOf(json.getAsString().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Failed to parse JSON, unexpected value " + json);
                return Priority.BASE;
            }
        }
    }
}

