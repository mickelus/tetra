package se.mickelus.tetra.module.schematic.requirement;

import com.google.gson.*;
import se.mickelus.mutil.util.JsonOptional;

import java.lang.reflect.Type;
import java.util.function.Predicate;

public class IntegerPredicate implements Predicate<Integer> {
    Integer min;
    Integer max;

    public IntegerPredicate(Integer min, Integer max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean test(Integer value) {
        if (min != null && min > value) {
            return false;
        }

        if (max != null && max < value) {
            return false;
        }

        return true;
    }

    public static class Deserializer implements JsonDeserializer<IntegerPredicate> {
        @Override
        public IntegerPredicate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonObject()) {
                JsonObject jsonObject = json.getAsJsonObject();


                return new IntegerPredicate(
                        JsonOptional.field(jsonObject, "min")
                                .map(JsonElement::getAsInt)
                                .orElse(null),
                        JsonOptional.field(jsonObject, "max")
                                .map(JsonElement::getAsInt)
                                .orElse(null));
            }

            int value = json.getAsInt();
            return new IntegerPredicate(value, value);
        }
    }
}
