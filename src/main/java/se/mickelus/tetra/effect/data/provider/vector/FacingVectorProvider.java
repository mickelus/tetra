package se.mickelus.tetra.effect.data.provider.vector;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.condition.FixedItemEffectCondition;
import se.mickelus.tetra.effect.data.condition.ItemEffectCondition;
import se.mickelus.tetra.effect.data.provider.entity.EntityProvider;

public class FacingVectorProvider implements VectorProvider {
    EntityProvider entity;
    ItemEffectCondition cardinal = new FixedItemEffectCondition(false);
    ItemEffectCondition onlyHorizontal = new FixedItemEffectCondition(false);

    @Override
    public Vec3 getVector(ItemEffectContext context) {
        boolean onlyHorizontalValue = onlyHorizontal.test(context);

        if (cardinal.test(context)) {
            if (onlyHorizontalValue) {
                return Vec3.atLowerCornerOf(entity.getEntity(context).getDirection().getNormal());
            }

            Vec3 lookAngle = entity.getEntity(context).getLookAngle();
            return Vec3.atLowerCornerOf(Direction.getNearest(lookAngle.x, lookAngle.y, lookAngle.z).getNormal());
        }

        if (onlyHorizontalValue) {
            return entity.getEntity(context).getLookAngle().multiply(1, 0, 1).normalize();
        }

        return entity.getEntity(context).getLookAngle();
    }
}
