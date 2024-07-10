package se.mickelus.tetra.effect.data.provider;

import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.effect.data.ItemEffectContext;

public class EffectLevelNumberProvider implements NumberProvider {
    private ItemEffect effect;
    @Override
    public float getValue(ItemEffectContext context) {
        return getIntegerValue(context);
    }

    @Override
    public int getIntegerValue(ItemEffectContext context) {
        return EffectHelper.getEffectLevel(context.getUsedItemStack(), effect);
    }
}
