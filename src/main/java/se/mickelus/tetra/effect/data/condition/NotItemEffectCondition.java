package se.mickelus.tetra.effect.data.condition;

import se.mickelus.tetra.effect.data.ItemEffectContext;

public class NotItemEffectCondition extends ItemEffectCondition {
    private ItemEffectCondition condition;

    @Override
    public boolean test(ItemEffectContext context) {
        return !condition.test(context);
    }
}
