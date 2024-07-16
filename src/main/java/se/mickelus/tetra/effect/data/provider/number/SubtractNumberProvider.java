package se.mickelus.tetra.effect.data.provider.number;

import se.mickelus.tetra.effect.data.ItemEffectContext;

public class SubtractNumberProvider implements NumberProvider {
    private final NumberProvider positive;
    private final NumberProvider negative;

    public SubtractNumberProvider(NumberProvider positive, NumberProvider negative) {
        this.positive = positive;
        this.negative = negative;
    }

    @Override
    public float getValue(ItemEffectContext context) {
        return positive.getValue(context) - negative.getValue(context);
    }
}
