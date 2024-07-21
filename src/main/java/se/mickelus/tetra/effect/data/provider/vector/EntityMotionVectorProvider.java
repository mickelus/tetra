package se.mickelus.tetra.effect.data.provider.vector;

import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.entity.EntityProvider;

public class EntityMotionVectorProvider implements VectorProvider {
    EntityProvider entity;

    @Override
    public Vec3 getVector(ItemEffectContext context) {
        return entity.getEntity(context).getDeltaMovement();
    }
}
