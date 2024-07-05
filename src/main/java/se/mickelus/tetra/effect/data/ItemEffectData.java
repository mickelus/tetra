package se.mickelus.tetra.effect.data;

import java.util.Map;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.effect.data.condition.ItemEffectCondition;
import se.mickelus.tetra.effect.data.outcome.ItemEffectOutcome;
import se.mickelus.tetra.effect.data.provider.NumberProvider;

public class ItemEffectData {
    public ItemEffect effect;
    public Map<String, NumberProvider> data;
    public ItemEffectTrigger trigger;
    public ItemEffectCondition condition;
    public ItemEffectOutcome outcome;
}
