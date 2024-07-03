package se.mickelus.tetra.effect.data.provider;

import se.mickelus.tetra.effect.data.ItemEffectContext;

public interface NumberProvider {
    float getValue(ItemEffectContext context);

    default int getIntegerValue(ItemEffectContext context) {
        return Math.round(getValue(context));
    }
}
