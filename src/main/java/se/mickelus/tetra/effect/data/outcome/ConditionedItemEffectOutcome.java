package se.mickelus.tetra.effect.data.outcome;

import com.google.gson.JsonObject;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.condition.ItemEffectCondition;

public class ConditionedItemEffectOutcome extends ItemEffectOutcome {
    ItemEffectOutcome outcome;
    ItemEffectCondition condition;

    @Override
    public boolean perform(ItemEffectContext context) {
        if (condition.test(context)) {
            return outcome.perform(context);
        }
        return false;
    }

    public static ItemEffectOutcome deserialize(JsonObject jsonObject) {
        return DataManager.gson.fromJson(jsonObject, ConditionedItemEffectOutcome.class);
    }
}
