package se.mickelus.tetra.effect.data.provider.number;

import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.condition.FixedItemEffectCondition;
import se.mickelus.tetra.effect.data.condition.ItemEffectCondition;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;

public class LengthNumberProvider implements NumberProvider {
    VectorProvider vector;
    ItemEffectCondition square = new FixedItemEffectCondition(true);

    @Override
    public float getValue(ItemEffectContext context) {
        return (float) (square.test(context)
                ? vector.getVector(context).lengthSqr()
                : vector.getVector(context).length());
    }
}
