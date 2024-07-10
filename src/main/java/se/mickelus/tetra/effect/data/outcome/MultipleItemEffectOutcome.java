package se.mickelus.tetra.effect.data.outcome;

import com.google.gson.JsonObject;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.NumberProvider;
import se.mickelus.tetra.util.StreamHelper;

import java.util.Arrays;
import java.util.List;

public class MultipleItemEffectOutcome extends ItemEffectOutcome {
    ItemEffectOutcome[] outcomes;
    boolean random = false;
    boolean onlyCountSuccessful = false;
    NumberProvider count = (context) -> Integer.MAX_VALUE;

    @Override
    public boolean perform(ItemEffectContext context) {
        List<ItemEffectOutcome> applicableOutcomes = random
                ? Arrays.stream(outcomes).collect(StreamHelper.toShuffledList())
                : Arrays.asList(outcomes);

        int limit = this.count.getIntegerValue(context);
        int count = 0;
        boolean anySucess = false;
        for (int i = 0; i < applicableOutcomes.size() & count < limit; i++) {
            boolean wasSuccess = applicableOutcomes.get(i).perform(context);
            if (!onlyCountSuccessful || wasSuccess) {
                count++;
            }
            if (wasSuccess) {
                anySucess = true;
            }
        }
        return anySucess;
    }

    public static ItemEffectOutcome deserialize(JsonObject jsonObject) {
        return DataManager.gson.fromJson(jsonObject, MultipleItemEffectOutcome.class);
    }
}
