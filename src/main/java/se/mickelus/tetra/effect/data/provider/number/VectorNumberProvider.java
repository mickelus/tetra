package se.mickelus.tetra.effect.data.provider.number;

import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;

public class VectorNumberProvider implements NumberProvider {
    VectorProvider vector;
    Field field;

    @Override
    public float getValue(ItemEffectContext context) {
        return switch (field) {
            case x -> (float) vector.getVector(context).x();
            case y -> (float) vector.getVector(context).y();
            case z -> (float) vector.getVector(context).z();
        };
    }

    public enum Field {
        x,
        y,
        z
    }
}
