package se.mickelus.tetra.effect.data.condition;

import se.mickelus.tetra.effect.data.ItemEffectContext;

import java.util.Arrays;

public class OrItemEffectCondition extends ItemEffectCondition {
    private ItemEffectCondition[] conditions;

    @Override
    public boolean test(ItemEffectContext context) {
        return Arrays.stream(conditions).anyMatch(condition -> condition.test(context));
    }
}
