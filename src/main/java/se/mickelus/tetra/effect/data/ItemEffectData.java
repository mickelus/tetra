package se.mickelus.tetra.effect.data;

import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.effect.data.condition.ItemEffectCondition;
import se.mickelus.tetra.effect.data.outcome.ItemEffectOutcome;
import se.mickelus.tetra.effect.data.provider.number.NumberProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ItemEffectData {
    public ItemEffect effect;
    public Map<String, NumberProvider> data;
    public ItemEffectTrigger trigger;
    public ItemEffectCondition condition;
    public ItemEffectOutcome outcome;

    public static Map<String, Float> calculateData(ItemEffectData effectData, ItemEffectContext context) {
        if (effectData.data != null) {
            return calculateData(effectData.data, context);
        }
        return Collections.emptyMap();
    }

    public static Map<String, Float> calculateData(Map<String, NumberProvider> data, ItemEffectContext context) {
        Map<String, Float> result = new HashMap<>();
        ItemEffectContext updatedContext = context;
        for (Map.Entry<String, NumberProvider> entry : data.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getValue(updatedContext));
            updatedContext = updatedContext.withData(result);
        }
        return result;
    }
}
