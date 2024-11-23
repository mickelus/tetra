package se.mickelus.tetra.effect.data.outcome;

import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.condition.ItemEffectCondition;
import se.mickelus.tetra.effect.data.provider.number.NumberProvider;
import se.mickelus.tetra.util.StreamHelper;

import java.util.Arrays;
import java.util.List;

public class MultipleItemEffectOutcome extends ItemEffectOutcome {
    ItemEffectOutcome[] outcomes;
    ItemEffectCondition random;
    ItemEffectCondition onlyCountSuccessful;
    ItemEffectCondition stagger;
    NumberProvider count = (context) -> Integer.MAX_VALUE;
    NumberProvider failureLimit = (context) -> Integer.MAX_VALUE;

    @Override
    public boolean perform(ItemEffectContext context) {
        List<ItemEffectOutcome> applicableOutcomes = random != null && random.test(context)
                ? Arrays.stream(outcomes).collect(StreamHelper.toShuffledList())
                : Arrays.asList(outcomes);

        int limit = this.count.getIntegerValue(context);
        int count = 0;
        int failureCount = 0;
        boolean anySucess = false;
        for (int i = 0; i < applicableOutcomes.size() & count < limit; i++) {
            boolean wasSuccess = applicableOutcomes.get(i).perform(context);
            if (!(onlyCountSuccessful != null && onlyCountSuccessful.test(context)) || wasSuccess) {
                count++;
            }
            if (wasSuccess) {
                anySucess = true;
            } else {
                failureCount++;
                if (failureCount >= failureLimit.getIntegerValue(context)) {
                    return false;
                }
            }
        }
        return anySucess;
    }
}
