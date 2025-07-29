package se.mickelus.tetra.effect.modifier;

import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.effect.data.ItemEffectData;
import se.mickelus.tetra.effect.data.condition.ItemEffectCondition;
import se.mickelus.tetra.effect.data.provider.number.NumberProvider;
import se.mickelus.tetra.module.Priority;

public record ModifierEffect(ModifierType type, ItemEffect effect, Priority priority, ItemEffectData.Data data, ItemEffectCondition condition,
        NumberProvider result) {

    public ModifierEffect {
        if (type == null) {
            throw new IllegalArgumentException("ModifierEffect type cannot be null");
        }
        if (effect == null) {
            throw new IllegalArgumentException("ModifierEffect effect cannot be null");
        }
        if (priority == null) {
            priority = Priority.BASE;
        }
    }
}
