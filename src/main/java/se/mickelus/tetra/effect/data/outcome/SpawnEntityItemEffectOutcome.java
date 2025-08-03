package se.mickelus.tetra.effect.data.outcome;

import com.google.common.collect.ImmutableMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.condition.FixedItemEffectCondition;
import se.mickelus.tetra.effect.data.condition.ItemEffectCondition;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;

public class SpawnEntityItemEffectOutcome extends ItemEffectOutcome {
    CompoundTag entity;
    VectorProvider position;
    ItemEffectCondition playSound = new FixedItemEffectCondition(true);
    String outcomeEntityKey = "spawnedEntity";
    ItemEffectOutcome outcome;

    @Override
    public boolean perform(ItemEffectContext context) {
        Vec3 resolvedPosition = position.getVector(context);
        Entity entityInstance = EntityType.loadEntityRecursive(entity, context.getLevel(), e -> {
            e.moveTo(resolvedPosition.x(), resolvedPosition.y(), resolvedPosition.z());
            return e;
        });
        if (entityInstance != null && context.getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.addFreshEntityWithPassengers(entityInstance);
            ItemEffectContext updatedContext = context.withMergedEntities(ImmutableMap.of(outcomeEntityKey, entityInstance));
            if (playSound.test(updatedContext) && entityInstance instanceof Mob mob) {
                mob.playAmbientSound();
            }
            if (outcome != null) {
                outcome.perform(updatedContext);
            }
            return true;
        }
        return false;
    }
}
