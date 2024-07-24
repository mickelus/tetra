package se.mickelus.tetra.effect.data.outcome;

import se.mickelus.tetra.effect.data.ItemEffectContext;

public class StaggerItemEffectOutcome extends ItemEffectOutcome {
    ItemEffectOutcome[] outcomes;

    @Override
    public boolean perform(ItemEffectContext context) {
        for (ItemEffectOutcome outcome : outcomes) {
            if (!outcome.perform(context)) {
                return false;
            }
        }
        return true;
    }
}
