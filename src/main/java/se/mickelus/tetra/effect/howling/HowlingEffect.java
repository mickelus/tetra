package se.mickelus.tetra.effect.howling;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.network.PacketHandler;
import se.mickelus.tetra.util.CastOptional;

import java.util.Optional;
import java.util.Random;

public class HowlingEffect {
    public static void sendPacket() {
        TetraMod.packetHandler.sendToServer(new HowlingPacket());
    }

    public static void trigger(ItemStack itemStack, LivingEntity player, int effectLevel) {
        int duration = (int) Math.round(EffectHelper.getEffectEfficiency(itemStack, ItemEffect.howling) * 20);
        int currentAmplifier = Optional.ofNullable(player.getEffect(HowlingPotionEffect.instance))
                .map(EffectInstance::getAmplifier)
                .orElse(-1);

        player.addEffect(new EffectInstance(HowlingPotionEffect.instance, duration, Math.min(currentAmplifier + effectLevel, 11), false, false));
    }

    public static void deflectProjectile(ProjectileImpactEvent event, ProjectileEntity projectile, RayTraceResult rayTraceResult) {
        Optional.ofNullable(rayTraceResult)
                .filter(result -> result.getType() == RayTraceResult.Type.ENTITY)
                .map(result -> (EntityRayTraceResult) result)
                .map(EntityRayTraceResult::getEntity)
                .flatMap(entity -> CastOptional.cast(entity, LivingEntity.class))
                .filter(entity -> willDeflect(entity.getEffect(HowlingPotionEffect.instance), entity.getRandom()))
                .ifPresent(entity -> {
                    Vector3d newDir;
                    if (entity.getEffect(HowlingPotionEffect.instance).getAmplifier() * 0.02 < entity.getRandom().nextDouble()) {
                        Vector3d normal = entity.position().add(0, entity.getBbHeight() / 2, 0).subtract(projectile.position()).normalize();
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

    private static boolean willDeflect(EffectInstance effectInstance, Random random) {
        return effectInstance != null && random.nextDouble() < effectInstance.getAmplifier() * 0.125;
    }
}
