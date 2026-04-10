package se.mickelus.tetra.effect.vexing;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.effect.potion.UnstablePowerMobEffect;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ParametersAreNonnullByDefault
public class VexingEffect {
    private static final Cache<UUID, Integer> currentVexCache = CacheBuilder.newBuilder()
        .maximumSize(100)
        .expireAfterWrite(40, TimeUnit.SECONDS)
        .build();

    private static boolean isInTimeout(LivingEntity entity) {
        return Optional.of(entity.getUUID())
            .map(currentVexCache::getIfPresent)
            .map(entity.level()::getEntity)
            .map(Entity::isAlive)
            .orElse(false);
    }

    private static void setCurrentVex(LivingEntity entity, Vex vex) {
        currentVexCache.put(entity.getUUID(), vex.getId());
    }

    public static void perform(LivingEntity entity, ItemStack itemStack, double multiplier) {
        if (!entity.level().isClientSide) {
            Level level = entity.level();
            double effectProbability = EffectHelper.getEffectEfficiency(itemStack, ItemEffect.vexing);
            if (effectProbability > 0
                && !isInTimeout(entity)
                && entity.getRandom().nextDouble() < effectProbability / 100 * multiplier) {
                int effectLevel = EffectHelper.getEffectLevel(itemStack, ItemEffect.vexing);
                BlockPos origin = entity.blockPosition();

                Vex vex = EntityType.VEX.create(level);
                if (vex != null) {
                    vex.setItemInHand(InteractionHand.MAIN_HAND, itemStack.copy());
                    vex.setDropChance(EquipmentSlot.MAINHAND, 0);
                    vex.setLimitedLife(160);
                    vex.moveTo(entity.getX(), entity.getY() + 1, entity.getZ(), entity.getYRot(), 0.0F);
                    vex.setIsCharging(true);
                    vex.finalizeSpawn((ServerLevelAccessor) level, level.getCurrentDifficultyAt(origin), MobSpawnType.MOB_SUMMONED, null);
                    vex.setBoundOrigin(origin);
                    vex.addEffect(new MobEffectInstance(EffectHelper.effectHolder(UnstablePowerMobEffect.instance), 1199, effectLevel - 1, false, false));

                    ((ServerLevelAccessor) level).addFreshEntityWithPassengers(vex);
                    vex.playAmbientSound();
                    setCurrentVex(entity, vex);
                }
            }
        }
    }
}
