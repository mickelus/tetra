package se.mickelus.tetra.effect.data.condition;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import se.mickelus.tetra.effect.data.ItemEffectContext;

import java.util.function.Function;

public class RandomItemEffectCondition extends ItemEffectCondition {
    public static final Codec<RandomItemEffectCondition> codec = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("chance").forGetter(i -> i.chance)
    ).apply(instance, RandomItemEffectCondition::new));
    public static Function<JsonElement, ItemEffectCondition> deserializer = jsonElement -> codec.parse(JsonOps.INSTANCE, jsonElement).result().orElse(null);

    public float chance;

    public RandomItemEffectCondition(float chance) {
        super();

        this.chance = chance;
    }

    @Override
    public boolean test(ItemEffectContext context) {
        return context.level().getRandom().nextFloat() < chance;
    }
}
