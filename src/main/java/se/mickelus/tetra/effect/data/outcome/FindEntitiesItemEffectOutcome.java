package se.mickelus.tetra.effect.data.outcome;

import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.world.phys.AABB;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.condition.FixedItemEffectCondition;
import se.mickelus.tetra.effect.data.condition.ItemEffectCondition;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;

import java.util.concurrent.atomic.AtomicBoolean;

public class FindEntitiesItemEffectOutcome extends ItemEffectOutcome {
    EntityPredicate predicate;
    VectorProvider origin;
    AABB bounds;
    ItemEffectOutcome outcome;
    ItemEffectCondition includeSource = new FixedItemEffectCondition(false);
    ItemEffectCondition includeTarget = new FixedItemEffectCondition(false);

    @Override
    public boolean perform(ItemEffectContext context) {
        AtomicBoolean result = new AtomicBoolean(false);
        boolean includeSourceValue = this.includeSource.test(context);
        boolean includeTargetValue = this.includeTarget.test(context);
        context.getLevel().getEntities(context.getUsingEntity(), bounds.move(origin.getBlockPos(context)),
                        entity -> (!includeSourceValue || entity.equals(context.getUsingEntity()))
                                && (!includeTargetValue || entity.equals(context.getTargetEntity()))
                                && predicate.matches(context.getLevel(), null, entity))
                .forEach(entity -> {
                    ItemEffectContext updatedContext = context.withTarget(entity);
                    boolean success = outcome.perform(updatedContext);
                    if (success) {
                        result.set(true);
                    }
                });
        return result.get();
    }
}
