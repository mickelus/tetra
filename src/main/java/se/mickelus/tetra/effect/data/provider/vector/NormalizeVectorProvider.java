package se.mickelus.tetra.effect.data.provider.vector;

import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.effect.data.ItemEffectContext;

public class NormalizeVectorProvider implements VectorProvider {
    VectorProvider vector;

    @Override
    public Vec3 getVector(ItemEffectContext context) {
        return vector.getVector(context).normalize();
    }
}
