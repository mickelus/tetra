package se.mickelus.tetra.effect.data.provider;

import se.mickelus.tetra.effect.data.ItemEffectContext;

public class DivideNumberProvider implements NumberProvider {
    private final NumberProvider numerator;
    private final NumberProvider denominator;

    public DivideNumberProvider(NumberProvider numerator, NumberProvider denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    @Override
    public float getValue(ItemEffectContext context) {
        return numerator.getValue(context) / denominator.getValue(context);
    }
}
