package se.mickelus.tetra.effect.data.provider.number;

import se.mickelus.tetra.effect.data.ItemEffectContext;

import java.util.Arrays;

public class SumNumberProvider implements NumberProvider {
    private final NumberProvider[] providers;

    public SumNumberProvider(NumberProvider... providers) {
        this.providers = providers;
    }

    @Override
    public float getValue(ItemEffectContext context) {
        return (float) Arrays.stream(providers).mapToDouble(provider -> provider.getValue(context)).sum();
    }
}
