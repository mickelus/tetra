package se.mickelus.tetra.effect.data.provider.vector;

import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.number.NumberProvider;

public class NumberVectorProvider implements VectorProvider {
    private NumberProvider x;
    private NumberProvider y;
    private NumberProvider z;

    public NumberVectorProvider(NumberProvider x, NumberProvider y, NumberProvider z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public Vec3 getVector(ItemEffectContext context) {
        return new Vec3(x.getValue(context), y.getValue(context), z.getValue(context));
    }
}
