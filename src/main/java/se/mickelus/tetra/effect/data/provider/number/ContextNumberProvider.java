package se.mickelus.tetra.effect.data.provider.number;

import se.mickelus.tetra.effect.data.ItemEffectContext;

import java.util.Optional;

public class ContextNumberProvider implements NumberProvider {
    String key;
    NumberProvider fallback = new FixedNumberProvider(0);

    public ContextNumberProvider(String key) {
        this.key = key;
    }

    @Override
    public float getValue(ItemEffectContext context) {
        return Optional.ofNullable(context.getNumbers().get(key))
                .orElseGet(() -> fallback.getValue(context));
    }
}
