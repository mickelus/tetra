package se.mickelus.tetra.effect.data.outcome;

import com.google.common.collect.ImmutableMap;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.condition.ItemEffectCondition;
import se.mickelus.tetra.effect.data.provider.number.NumberProvider;

public class LoopItemEffectOutcome extends ItemEffectOutcome {
    NumberProvider count;
    String indexKey;
    ItemEffectCondition breakCondition;
    ItemEffectOutcome outcome;

    @Override
    public boolean perform(ItemEffectContext context) {
        boolean anySuccess = false;
        int countValue = count.getIntegerValue(context);
        for (int i = 0; i < countValue; i++) {
            ItemEffectContext updatedContext = indexKey != null
                    ? context.withMergedNumbers(ImmutableMap.of(indexKey, (float) i))
                    : context;
            if (breakCondition != null && !breakCondition.test(updatedContext)) {
                return anySuccess;
            }
            if (outcome.perform(updatedContext)) {
                anySuccess = true;
            }
        }
        return anySuccess;
    }
}
