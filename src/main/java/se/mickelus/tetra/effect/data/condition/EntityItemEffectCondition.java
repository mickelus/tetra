package se.mickelus.tetra.effect.data.condition;

import net.minecraft.advancements.critereon.EntityPredicate;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.entity.EntityProvider;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;

public class EntityItemEffectCondition extends ItemEffectCondition {
    EntityProvider entity;
    VectorProvider origin;

    EntityPredicate condition;

    ItemEffectCondition canFreeze;
    ItemEffectCondition isFreezing;
    ItemEffectCondition isFrozen;

    @Override
    public boolean test(ItemEffectContext context) {
        if (condition != null && !condition.matches(context.getLevel(), origin != null ? origin.getVector(context) : null, entity.getEntity(context))) {
            return false;
        }
        if (canFreeze != null && canFreeze.test(context) != entity.getEntity(context).canFreeze()) {
            return false;
        }
        if (isFreezing != null && isFreezing.test(context) != entity.getEntity(context).isFreezing()) {
            return false;
        }
        if (isFrozen != null && isFrozen.test(context) != entity.getEntity(context).isFullyFrozen()) {
            return false;
        }

        return true;
    }
}
