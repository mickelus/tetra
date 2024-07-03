package se.mickelus.tetra.effect.data.provider;

import se.mickelus.tetra.effect.data.ItemEffectContext;

import java.util.Arrays;

public class MultiplyNumberProvider implements NumberProvider {
    private final NumberProvider[] providers;

    public MultiplyNumberProvider(NumberProvider... providers) {
        this.providers = providers;
    }

    @Override
    public float getValue(ItemEffectContext context) {
        return Arrays.stream(providers).map(provider -> provider.getValue(context)).reduce(1f, (a, b) -> a * b);
    }
}
