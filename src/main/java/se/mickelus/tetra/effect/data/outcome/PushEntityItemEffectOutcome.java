package se.mickelus.tetra.effect.data.outcome;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.entity.EntityProvider;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;

public class PushEntityItemEffectOutcome extends ItemEffectOutcome {
    VectorProvider vector;
    EntityProvider entity;

    @Override
    public boolean perform(ItemEffectContext context) {
        Vec3 vectorValue = vector.getVector(context);
        Entity entityValue = entity.getEntity(context);
        if (entityValue.isPushable()) {
            entityValue.push(vectorValue.x, vectorValue.y, vectorValue.z);
            return true;
        }
        return false;
    }
}
