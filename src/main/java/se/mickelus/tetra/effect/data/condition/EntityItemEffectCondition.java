package se.mickelus.tetra.effect.data.condition;

import net.minecraft.advancements.critereon.EntityPredicate;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.entity.EntityProvider;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;

public class EntityItemEffectCondition extends ItemEffectCondition {
    EntityPredicate condition;
    EntityProvider entity;
    VectorProvider origin;

    @Override
    public boolean test(ItemEffectContext context) {
        return condition.matches(context.getLevel(), origin != null ? origin.getVector(context) : null, entity.getEntity(context));
    }
}
