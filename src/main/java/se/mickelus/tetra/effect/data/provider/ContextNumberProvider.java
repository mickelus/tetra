package se.mickelus.tetra.effect.data.provider;

import se.mickelus.tetra.effect.data.ItemEffectContext;

public class ContextNumberProvider implements NumberProvider {
    String key;

    public ContextNumberProvider(String key) {
        this.key = key;
    }

    @Override
    public float getValue(ItemEffectContext context) {
        return 0;
    }
}
