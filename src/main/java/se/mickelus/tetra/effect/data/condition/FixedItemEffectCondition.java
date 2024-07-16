package se.mickelus.tetra.effect.data.condition;

import se.mickelus.tetra.effect.data.ItemEffectContext;

public class FixedItemEffectCondition extends ItemEffectCondition {
    Boolean value;

    public FixedItemEffectCondition(boolean value) {
        this.value = value;
    }

    @Override
    public boolean test(ItemEffectContext context) {
        return value;
    }
}
