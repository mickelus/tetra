package se.mickelus.tetra.module.schematic.requirement;

import com.google.gson.*;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
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
        if (min != null) {
            buffer.writeInt(min);
        }else {
            buffer.writeInt(-1);
        }
        if (max != null) {
            buffer.writeInt(max);
        }else {
            buffer.writeInt(-1);
        }
    }

    public static void writeNull(FriendlyByteBuf buffer) {
        buffer.writeInt(-1);
        buffer.writeInt(-1);
    }

    @Nullable
    public static IntegerPredicate fromBuffer(FriendlyByteBuf buffer) {
        int tierMin = buffer.readInt();
        int tierMax = buffer.readInt();
        return tierMin != -1 || tierMax != -1
                ? new IntegerPredicate(tierMin != -1 ? tierMin : null, tierMax != -1 ? tierMax : null)
                : null;
    }


    @OnlyIn(Dist.CLIENT)
    @Nullable
    public String getDescription() {
        return getDescription(I18n.get("tetra.integer_predicate_value"));
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    public String getDescription(String key) {
        if (min != null && max != null) {
            return I18n.get("tetra.integer_predicate_both", min, key, max);
        }
        if (min != null) {
            return I18n.get("tetra.integer_predicate_min", key, min);
        }
        if (max != null) {
            return I18n.get("tetra.integer_predicate_max", key, max);
        }
        return "BROKEN PREDICATE";
    }

    public static class Deserializer implements JsonDeserializer<IntegerPredicate> {

        private static int getLevel(JsonElement element) {
            if (element.getAsJsonPrimitive().isNumber()) {
                return element.getAsInt();
            }

            return Optional.ofNullable(TierSortingRegistry.byName(ResourceLocation.parse(element.getAsString())))
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
