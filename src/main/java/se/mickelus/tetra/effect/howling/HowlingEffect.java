package se.mickelus.tetra.effect.howling;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.mutil.util.CastOptional;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.Random;
@ParametersAreNonnullByDefault
public class HowlingEffect {
    public static void sendPacket() {
        TetraMod.packetHandler.sendToServer(new HowlingPacket());
    }

    public static void trigger(ItemStack itemStack, LivingEntity player, int effectLevel) {
        int duration = (int) Math.round(EffectHelper.getEffectEfficiency(itemStack, ItemEffect.howling) * 20);
        int currentAmplifier = Optional.ofNullable(player.getEffect(HowlingPotionEffect.instance))
                .map(MobEffectInstance::getAmplifier)
                .orElse(-1);

        player.addEffect(new MobEffectInstance(HowlingPotionEffect.instance, duration, Math.min(currentAmplifier + effectLevel, 11), false, false));
    }

    public static void deflectProjectile(ProjectileImpactEvent event, Projectile projectile, HitResult rayTraceResult) {
        Optional.ofNullable(rayTraceResult)
                .filter(result -> result.getType() == HitResult.Type.ENTITY)
                .map(result -> (EntityHitResult) result)
                .map(EntityHitResult::getEntity)
                .flatMap(entity -> CastOptional.cast(entity, LivingEntity.class))
                .filter(entity -> willDeflect(entity.getEffect(HowlingPotionEffect.instance), entity.getRandom()))
                .ifPresent(entity -> {
                    Vec3 newDir;
                    if (entity.getEffect(HowlingPotionEffect.instance).getAmplifier() * 0.02 < entity.getRandom().nextDouble()) {
                        Vec3 normal = entity.position().add(0, entity.getBbHeight() / 2, 0).subtract(projectile.position()).normalize();
                        newDir = projectile.getDeltaMovement().subtract(normal.scale(2 * projectile.getDeltaMovement().dot(normal)));
                    } else {
                        newDir = projectile.getDeltaMovement().scale(-0.8);
                        CastOptional.cast(projectile.getOwner(), LivingEntity.class)
                                .ifPresent(shooter -> shooter.setLastHurtMob(entity));
                        projectile.setOwner(entity);
                    }
//                    projectile.setMotion(newDir.scale(0.7f));
                    projectile.shoot(newDir.x, newDir.y, newDir.z, (float) projectile.getDeltaMovement().length(), 0.1f);
                    event.setCanceled(true);
                });
    }

    private static boolean willDeflect(MobEffectInstance effectInstance, Random random) {
        return effectInstance != null && random.nextDouble() < effectInstance.getAmplifier() * 0.125;
    }
}
