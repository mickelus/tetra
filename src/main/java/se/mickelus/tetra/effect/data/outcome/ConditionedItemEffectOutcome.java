package se.mickelus.tetra.effect.data.outcome;

import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.condition.ItemEffectCondition;

public class ConditionedItemEffectOutcome extends ItemEffectOutcome {
    ItemEffectOutcome outcome;
    ItemEffectOutcome otherwise;
    ItemEffectCondition condition;

    @Override
    public boolean perform(ItemEffectContext context) {
        if (condition.test(context)) {
            return outcome.perform(context);
        }
        if (otherwise != null) {
            return otherwise.perform(context);
        }
        return false;
    }
}
