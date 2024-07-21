package se.mickelus.tetra.effect.data.condition;

import net.minecraft.advancements.critereon.EntityPredicate;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.entity.EntityProvider;
import se.mickelus.tetra.effect.data.provider.entity.StandardEntityProvider;
import se.mickelus.tetra.effect.data.provider.vector.EntityPositionVectorProvider;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;

public class EntityItemEffectCondition extends ItemEffectCondition {
    EntityPredicate predicate;
    EntityProvider entity;
    VectorProvider origin = new EntityPositionVectorProvider(new StandardEntityProvider(StandardEntityProvider.Target.source));

    @Override
    public boolean test(ItemEffectContext context) {
        return predicate.matches(context.getLevel(), origin.getVector(context), entity.getEntity(context));
    }
}
