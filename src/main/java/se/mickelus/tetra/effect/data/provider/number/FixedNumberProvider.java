package se.mickelus.tetra.effect.data.provider.number;

import se.mickelus.tetra.effect.data.ItemEffectContext;

public class FixedNumberProvider implements NumberProvider {
    private float value;
    private int intValue;

    public FixedNumberProvider(float value) {
        this.value = value;
        this.intValue = Math.round(value);
    }

    @Override
    public float getValue(ItemEffectContext context) {
        return value;
    }

    @Override
    public int getIntegerValue(ItemEffectContext context) {
        return intValue;
    }
}
