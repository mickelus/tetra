package se.mickelus.tetra.effect.data;

import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.effect.data.condition.ItemEffectCondition;
import se.mickelus.tetra.effect.data.outcome.ItemEffectOutcome;

public class ItemEffectData {
    public ItemEffect effect;
    public ItemEffectTrigger trigger;
    public ItemEffectCondition condition;
    public ItemEffectOutcome outcome;
}
