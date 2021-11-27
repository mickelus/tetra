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
        int currentAmplifier = Optional.ofNullable(player.getActivePotionEffect(HowlingPotionEffect.instance))
                .map(EffectInstance::getAmplifier)
                .orElse(-1);

        player.addPotionEffect(new EffectInstance(HowlingPotionEffect.instance, duration, Math.min(currentAmplifier + effectLevel, 11), false, false));
    }

    public static void deflectProjectile(ProjectileImpactEvent event, ProjectileEntity projectile, RayTraceResult rayTraceResult) {
        Optional.ofNullable(rayTraceResult)
                .filter(result -> result.getType() == RayTraceResult.Type.ENTITY)
                .map(result -> (EntityRayTraceResult) result)
                .map(EntityRayTraceResult::getEntity)
                .flatMap(entity -> CastOptional.cast(entity, LivingEntity.class))
                .filter(entity -> willDeflect(entity.getActivePotionEffect(HowlingPotionEffect.instance), entity.getRNG()))
                .ifPresent(entity -> {
                    Vector3d newDir;
                    if (entity.getActivePotionEffect(HowlingPotionEffect.instance).getAmplifier() * 0.02 < entity.getRNG().nextDouble()) {
                        Vector3d normal = entity.getPositionVec().add(0, entity.getHeight() / 2, 0).subtract(projectile.getPositionVec()).normalize();
                        newDir = projectile.getMotion().subtract(normal.scale(2 * projectile.getMotion().dotProduct(normal)));
                    } else {
                        newDir = projectile.getMotion().scale(-0.8);
                        CastOptional.cast(projectile.func_234616_v_(), LivingEntity.class)
                                .ifPresent(shooter -> shooter.setLastAttackedEntity(entity));
                        projectile.setShooter(entity);
                    }
//                    projectile.setMotion(newDir.scale(0.7f));
                    projectile.shoot(newDir.x, newDir.y, newDir.z, (float) projectile.getMotion().length(), 0.1f);
                    event.setCanceled(true);
                });
    }

    private static boolean willDeflect(EffectInstance effectInstance, Random random) {
        return effectInstance != null && random.nextDouble() < effectInstance.getAmplifier() * 0.125;
    }
}
