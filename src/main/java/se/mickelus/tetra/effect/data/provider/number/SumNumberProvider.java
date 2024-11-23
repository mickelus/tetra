package se.mickelus.tetra.effect.data.provider.number;

import se.mickelus.tetra.effect.data.ItemEffectContext;

import java.util.Arrays;

public class SumNumberProvider implements NumberProvider {
    private final NumberProvider[] values;

    public SumNumberProvider(NumberProvider... providers) {
        this.values = providers;
    }

    @Override
    public float getValue(ItemEffectContext context) {
        return (float) Arrays.stream(values).mapToDouble(provider -> provider.getValue(context)).sum();
    }
}
