package se.mickelus.tetra.effect;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import se.mickelus.tetra.network.PacketHandler;
import se.mickelus.tetra.util.CastOptional;

import java.util.Optional;

public class SweepingEffect {

    /**
     * Perfoms a sweeping attack, dealing damage and playing effects similar to vanilla swords.
     * @param itemStack the itemstack used for the attack
     * @param target the attacking entity
     * @param attacker the attacked entity
     * @param sweepingLevel the level of the sweeping effect of the itemstack
     */
    public static void sweepAttack(ItemStack itemStack, LivingEntity target, LivingEntity attacker, int sweepingLevel) {
        boolean trueSweep = EffectHelper.getEffectLevel(itemStack, ItemEffect.truesweep) > 0;
        float damage = (float) Math.max(attacker.getAttributeValue(Attributes.ATTACK_DAMAGE) * (sweepingLevel * 0.125f), 1);
        float knockback = trueSweep ? (EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, itemStack) + 1) * 0.5f : 0.5f;
        double range = 1 + EffectHelper.getEffectEfficiency(itemStack, ItemEffect.sweeping);
        double reach = attacker.getAttributeValue(ForgeMod.REACH_DISTANCE.get());

        // range values set up to mimic vanilla behaviour
        attacker.world.getEntitiesWithinAABB(LivingEntity.class, target.getBoundingBox().grow(range, 0.25d, range)).stream()
                .filter(entity -> entity != attacker)
                .filter(entity -> entity != target)
                .filter(entity -> !attacker.isOnSameTeam(entity))
                .filter(entity -> attacker.getDistanceSq(entity) < range * range)
                .forEach(entity -> {
                    entity.applyKnockback(knockback,
                            MathHelper.sin(attacker.rotationYaw * (float) Math.PI / 180F),
                            -MathHelper.cos(attacker.rotationYaw * (float) Math.PI / 180F));

                    DamageSource damageSource = attacker instanceof PlayerEntity
                            ? DamageSource.causePlayerDamage((PlayerEntity) attacker) : DamageSource.causeIndirectDamage(attacker, entity);

                    if (trueSweep) {
                        ItemEffectHandler.applyHitEffects(itemStack, entity, attacker);
                        EffectHelper.applyEnchantmentHitEffects(itemStack, entity, attacker);

                        causeTruesweepDamage(damageSource, damage, itemStack, attacker, entity);
                    } else {
                        entity.attackEntityFrom(damageSource, damage);
                    }

                });

        attacker.world.playSound(null, attacker.getPosX(), attacker.getPosY(), attacker.getPosZ(),
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, attacker.getSoundCategory(), 1.0F, 1.0F);

        CastOptional.cast(attacker, PlayerEntity.class).ifPresent(PlayerEntity::spawnSweepParticles);
    }

    public static void triggerTruesweep() {
        PacketHandler.sendToServer(new TruesweepPacket());
    }

    /**
     * Perfoms a sweeping attack in front of the attacker without requiring a target, dealing damage to nearby entities and playing effects similar to vanilla swords.
     * @param itemStack the itemstack used for the attack
     * @param attacker the attacked entity
     */
    public static void truesweep(ItemStack itemStack, LivingEntity attacker) {
        int sweepingLevel = EffectHelper.getEffectLevel(itemStack, ItemEffect.sweeping);
        float damage = (float) Math.max(attacker.getAttributeValue(Attributes.ATTACK_DAMAGE) * (sweepingLevel * 0.125f), 1);
        float knockback = 0.5f + EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, itemStack) * 0.5f;
        double range = 2 + EffectHelper.getEffectEfficiency(itemStack, ItemEffect.sweeping);

        Vector3d target = Vector3d.fromPitchYaw(attacker.rotationPitch, attacker.rotationYaw)
                .normalize()
                .scale(range)
                .add(attacker.getEyePosition(0));
        AxisAlignedBB aoe = new AxisAlignedBB(target, target);

        // range values set up to mimic vanilla behaviour
        attacker.world.getEntitiesWithinAABB(LivingEntity.class, aoe.grow(range, 1d, range)).stream()
                .filter(entity -> entity != attacker)
                .filter(entity -> !attacker.isOnSameTeam(entity))
                .forEach(entity -> {
                    entity.applyKnockback(knockback,
                            MathHelper.sin(attacker.rotationYaw * (float) Math.PI / 180F),
                            -MathHelper.cos(attacker.rotationYaw * (float) Math.PI / 180F));

                    ItemEffectHandler.applyHitEffects(itemStack, entity, attacker);
                    EffectHelper.applyEnchantmentHitEffects(itemStack, entity, attacker);


                    DamageSource damageSource = attacker instanceof PlayerEntity
                            ? DamageSource.causePlayerDamage((PlayerEntity) attacker) : DamageSource.causeIndirectDamage(attacker, entity);
                    causeTruesweepDamage(damageSource, damage, itemStack, attacker, entity);
                });

        attacker.world.playSound(null, attacker.getPosX(), attacker.getPosY(), attacker.getPosZ(),
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, attacker.getSoundCategory(), 1.0F, 1.0F);

        CastOptional.cast(attacker, PlayerEntity.class).ifPresent(PlayerEntity::spawnSweepParticles);
    }

    private static void causeTruesweepDamage(DamageSource damageSource, float baseDamage, ItemStack itemStack, LivingEntity attacker, LivingEntity target) {
        float targetModifier = EnchantmentHelper.getModifierForCreature(itemStack, target.getCreatureAttribute());
        float critMultiplier = CastOptional.cast(attacker, PlayerEntity.class)
                .map(player -> ForgeHooks.getCriticalHit(player, target, false, 1.5f))
                .map(CriticalHitEvent::getDamageModifier)
                .orElse(1f);

        target.attackEntityFrom(damageSource, (baseDamage + targetModifier) * critMultiplier);

        if (targetModifier > 0) {
            CastOptional.cast(attacker, PlayerEntity.class).ifPresent(player -> player.onEnchantmentCritical(target));
        }

        if (critMultiplier > 1) {
            attacker.getEntityWorld().playSound(null, target.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1, 1.3f);
            ((PlayerEntity) attacker).onCriticalHit(target);
        }
    }
}
