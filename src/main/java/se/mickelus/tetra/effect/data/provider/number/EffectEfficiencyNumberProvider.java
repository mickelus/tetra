package se.mickelus.tetra.effect.data.provider.number;

import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.effect.data.ItemEffectContext;

import java.util.Optional;

public class EffectEfficiencyNumberProvider implements NumberProvider {
    private ItemEffect effect;
    NumberProvider fallback = new FixedNumberProvider(0);

    @Override
    public float getValue(ItemEffectContext context) {
        return Optional.of(EffectHelper.getEffectEfficiency(context.getUsedItemStack(), effect))
                .filter(level -> level != 0)
                .orElseGet(() -> fallback.getValue(context));
    }
}
