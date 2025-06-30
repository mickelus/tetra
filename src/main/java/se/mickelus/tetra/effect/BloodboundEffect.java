package se.mickelus.tetra.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.client.particle.DripParticles;

public class BloodboundEffect {
    public static int reduceDamage(ItemStack itemStack, LivingEntity entity, int amount) {
        if (entity != null && amount > 0) {
            int level = EffectHelper.getEffectLevel(itemStack, ItemEffect.bloodbound);
            float ratio = EffectHelper.getEffectEfficiency(itemStack, ItemEffect.bloodbound);
            if (level > 0 && ratio > 0 && entity.getRandom().nextFloat() < level / 100f) {
                float maxDrain = Math.max(0, entity.getHealth() - 1);
                if (maxDrain > 0) {
                    float maxNegate = maxDrain * ratio;
                    float negateAmount = Math.min(maxNegate, amount);
                    int roundedNegateAmount = Math.round(negateAmount);
                    float healthCost = negateAmount / ratio;
                    entity.setHealth(entity.getHealth() - healthCost);

                    if (!entity.level().isClientSide && entity.getRandom().nextFloat() < Math.max(0.1, 1 - (level / 100f))) {
                        spawnParticle(entity, itemStack);
                    }
                    
                    if (roundedNegateAmount > 0) {
                        return amount - roundedNegateAmount;
                    }
                }
            }
        }
        return amount;
    }

    private static void spawnParticle(LivingEntity entity, ItemStack itemStack) {
        float rotationOffset = itemStack.equals(entity.getOffhandItem()) ? -90 : 90;
        Vec3 target = Vec3.directionFromRotation(entity.getXRot(), entity.getYRot())
                .normalize().scale(0.2).add(Vec3.directionFromRotation(entity.getXRot(), entity.getYRot() + rotationOffset)
                        .normalize().scale(0.4));
        ((ServerLevel) entity.level()).sendParticles(DripParticles.fallingBlood.get(),
                entity.getX() + target.x(), entity.getY() + target.y() + entity.getEyeHeight() * 0.7, entity.getZ() + target.z(),
                0, 0, 0, 0, 0.01f);
    }
}

