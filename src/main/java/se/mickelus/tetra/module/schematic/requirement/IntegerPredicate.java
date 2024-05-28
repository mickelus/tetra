package se.mickelus.tetra.module.schematic.requirement;

import com.google.gson.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.TierSortingRegistry;
import se.mickelus.mutil.util.JsonOptional;
import se.mickelus.tetra.util.TierHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Optional;
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

    public JsonElement serialize() {
        JsonObject json = new JsonObject();
        if (min != null) {
            json.addProperty("min", min);
        }
        if (max != null) {
            json.addProperty("max", max);
        }
        return json;
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        if (min != Integer.MIN_VALUE) {
            buffer.writeInt(min);
        }
        if (max != Integer.MIN_VALUE) {
            buffer.writeInt(max);
        }
    }

    @Nullable
    public static IntegerPredicate fromBuffer(FriendlyByteBuf buffer) {
        int tierMin = buffer.readVarInt();
        int tierMax = buffer.readVarInt();
        return tierMin != Integer.MIN_VALUE || tierMax != Integer.MIN_VALUE
                ? new IntegerPredicate(tierMin != Integer.MIN_VALUE ? tierMin : null, tierMax != Integer.MIN_VALUE ? tierMax : null)
                : null;
    }

    public static class Deserializer implements JsonDeserializer<IntegerPredicate> {

        private static int getLevel(JsonElement element) {
            if (element.getAsJsonPrimitive().isNumber()) {
                return element.getAsInt();
            }

            return Optional.ofNullable(TierSortingRegistry.byName(new ResourceLocation(element.getAsString())))
                    .map(TierHelper::getIndex)
                    .map(index -> index + 1)
                    .orElse(0);
        }
        
        @Override
        public IntegerPredicate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return deserialize(json);
        }

        public static IntegerPredicate deserialize(JsonElement json) throws JsonParseException {
            if (json.isJsonObject()) {
                JsonObject jsonObject = json.getAsJsonObject();


                return new IntegerPredicate(
                        JsonOptional.field(jsonObject, "min")
                                .map(Deserializer::getLevel)
                                .orElse(null),
                        JsonOptional.field(jsonObject, "max")
                                .map(Deserializer::getLevel)
                                .orElse(null));
            }

            int value = json.getAsInt();
            return new IntegerPredicate(value, value);
        }
    }
}
