package se.mickelus.tetra.effect.data.provider.number;

import se.mickelus.tetra.effect.data.ItemEffectContext;

public class RandomNumberProvider implements NumberProvider {

    private NumberProvider min = new FixedNumberProvider(0);
    private NumberProvider max = new FixedNumberProvider(1);

    private boolean gaussian = false;

    public RandomNumberProvider() {
    }

    public RandomNumberProvider(NumberProvider min, NumberProvider max, boolean gaussian) {
        this.min = min;
        this.max = max;
        this.gaussian = gaussian;
    }

    @Override
    public float getValue(ItemEffectContext context) {
        float min = this.min.getValue(context);
        float max = this.max.getValue(context);
        if (gaussian) {
            return (float) context.getLevel().getRandom().nextGaussian() * (max - min) + min;
        }
        return context.getLevel().getRandom().nextFloat() * (max - min) + min;
    }

    @Override
    public int getIntegerValue(ItemEffectContext context) {
        return context.getLevel().getRandom().nextInt(min.getIntegerValue(context), max.getIntegerValue(context));
    }
}
