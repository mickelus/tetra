package se.mickelus.tetra.effect.data.condition;

import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.number.NumberProvider;

public class RandomItemEffectCondition extends ItemEffectCondition {
    public NumberProvider chance;

    public RandomItemEffectCondition(NumberProvider chance) {
        super();

        this.chance = chance;
    }

    @Override
    public boolean test(ItemEffectContext context) {
        return context.getLevel().getRandom().nextFloat() < chance.getValue(context);
    }
}
