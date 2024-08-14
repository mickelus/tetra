package se.mickelus.tetra.effect.data.provider.number;

import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.effect.data.ItemEffectContext;

import java.util.Optional;

public class EffectLevelNumberProvider implements NumberProvider {
    ItemEffect effect;
    NumberProvider fallback = new FixedNumberProvider(0);

    @Override
    public float getValue(ItemEffectContext context) {
        return getIntegerValue(context);
    }

    @Override
    public int getIntegerValue(ItemEffectContext context) {
        return Optional.of(EffectHelper.getEffectLevel(context.getUsedItemStack(), effect))
                .filter(level -> level > 0)
                .orElseGet(() -> fallback.getIntegerValue(context));
    }
}
