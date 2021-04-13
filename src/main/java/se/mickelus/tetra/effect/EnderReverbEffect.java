package se.mickelus.tetra.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.EndermiteEntity;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import se.mickelus.tetra.util.CastOptional;

import java.util.List;

public class EnderReverbEffect {
    public static void perform(LivingEntity entity, ItemStack itemStack, double multiplier) {
        if (!entity.world.isRemote) {
            double effectProbability = EffectHelper.getEffectEfficiency(itemStack, ItemEffect.enderReverb);
            if (effectProbability > 0
                    && !CastOptional.cast(entity, PlayerEntity.class).map(PlayerEntity::isCreative).orElse(false)
                    && entity.getRNG().nextDouble() < effectProbability * multiplier) {
                AxisAlignedBB aabb = new AxisAlignedBB(entity.getPosition()).grow(24);
                List<LivingEntity> nearbyTargets = entity.world.getEntitiesWithinAABB(LivingEntity.class, aabb,
                        target -> target instanceof EndermanEntity || target instanceof EndermiteEntity
                                || target instanceof ShulkerEntity || target instanceof EnderDragonEntity);
                if (nearbyTargets.size() > 0) {
                    nearbyTargets.get(entity.getRNG().nextInt(nearbyTargets.size())).setRevengeTarget(entity);
                }
            }
        }
    }
}
