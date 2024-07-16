package se.mickelus.tetra.effect.data.outcome;

import net.minecraft.world.entity.Entity;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.entity.EntityProvider;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;

public class MoveEntityItemEffectOutcome extends ItemEffectOutcome {
    EntityProvider entity;
    VectorProvider position;

    @Override
    public boolean perform(ItemEffectContext context) {
        Entity target = entity.getEntity(context);
        if (target != null) {
            target.setPos(position.getVector(context));
            return true;
        }
        return false;
    }
}
