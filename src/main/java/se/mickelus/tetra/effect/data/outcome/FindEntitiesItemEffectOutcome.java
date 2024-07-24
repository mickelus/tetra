package se.mickelus.tetra.effect.data.outcome;

import com.google.common.collect.ImmutableMap;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.entity.EntityProvider;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class FindEntitiesItemEffectOutcome extends ItemEffectOutcome {
    EntityPredicate predicate;
    VectorProvider origin;
    AABB bounds;
    ItemEffectOutcome outcome;

    EntityProvider[] exclude = new EntityProvider[0];

    @Override
    public boolean perform(ItemEffectContext context) {
        AtomicBoolean result = new AtomicBoolean(false);
        context.getLevel().getEntities((Entity) null, bounds.move(origin.getBlockPos(context)),
                        entity -> Arrays.stream(exclude).noneMatch(e -> entity.equals(e.getEntity(context)))
                                && predicate.matches(context.getLevel(), null, entity))
                .forEach(entity -> {
                    ItemEffectContext updatedContext = context.withMergedEntities(ImmutableMap.of("ref", entity));
                    boolean success = outcome.perform(updatedContext);
                    if (success) {
                        result.set(true);
                    }
                });
        return result.get();
    }
}
