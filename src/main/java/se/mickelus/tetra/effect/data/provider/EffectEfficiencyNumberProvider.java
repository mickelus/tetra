package se.mickelus.tetra.effect.data.provider;

import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.effect.data.ItemEffectContext;

public class EffectEfficiencyNumberProvider implements NumberProvider {
    private ItemEffect effect;

    @Override
    public float getValue(ItemEffectContext context) {
        return EffectHelper.getEffectEfficiency(context.getUsedItemStack(), effect);
    }
}
