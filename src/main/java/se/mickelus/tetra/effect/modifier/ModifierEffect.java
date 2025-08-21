package se.mickelus.tetra.effect.modifier;

import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.effect.data.ItemEffectData;
import se.mickelus.tetra.effect.data.condition.ItemEffectCondition;
import se.mickelus.tetra.effect.data.provider.number.NumberProvider;
import se.mickelus.tetra.module.Priority;

public class ModifierEffect {

    public ModifierType type;
    public ItemEffect effect;
    public Priority priority = Priority.BASE;
    public ItemEffectData.Data data;
    public ItemEffectCondition condition;
    public NumberProvider result;

    public String key;
}
