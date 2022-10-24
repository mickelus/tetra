package se.mickelus.tetra.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;
import se.mickelus.tetra.ServerScheduler;

import java.util.List;

public class JankEffect {
    public static void jankItemsDelayed(ServerLevel level, BlockPos target, int effectLevel, float efficiency, Entity entity) {
        ServerScheduler.schedule(0, () -> jankItems(level, target, effectLevel, efficiency, entity));
    }

    public static void jankItems(ServerLevel level, BlockPos target, int effectLevel, float efficiency, Entity entity) {
        List<ItemEntity> items = level.getEntities(EntityType.ITEM, new AABB(target).inflate(effectLevel * 0.5), Entity::isAlive);

        if (!items.isEmpty() && level.random.nextFloat() < efficiency && level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            Endermite endermite = EntityType.ENDERMITE.create(level);
            endermite.moveTo(target, 0, entity.getXRot() + 180);
            level.addFreshEntity(endermite);
        }

        items.forEach(item -> {
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, item.getX() + item.getBbWidth() / 2, item.getY() + item.getBbHeight() / 2,
                    item.getZ() + item.getBbWidth() / 2, 1, 0, 0, 0, 0);
            item.moveTo(entity.getPosition(0));
            item.setPickUpDelay(0);
        });

        level.getEntities(EntityType.EXPERIENCE_ORB, new AABB(target).inflate(effectLevel * 0.5), Entity::isAlive).forEach(orb -> orb.moveTo(entity.getPosition(0)));
    }
}
