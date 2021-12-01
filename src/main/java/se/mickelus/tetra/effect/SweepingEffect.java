package se.mickelus.tetra.effect;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.util.CastOptional;

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
        float knockback = trueSweep ? (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.KNOCKBACK, itemStack) + 1) * 0.5f : 0.5f;
        double range = 1 + EffectHelper.getEffectEfficiency(itemStack, ItemEffect.sweeping);
        double reach = attacker.getAttributeValue(ForgeMod.REACH_DISTANCE.get());

        // range values set up to mimic vanilla behaviour
        attacker.level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(range, 0.25d, range)).stream()
                .filter(entity -> entity != attacker)
                .filter(entity -> entity != target)
                .filter(entity -> !attacker.isAlliedTo(entity))
                .filter(entity -> attacker.distanceToSqr(entity) < (range + reach) * (range + reach))
                .forEach(entity -> {
                    entity.knockback(knockback,
                            Mth.sin(attacker.yRot * (float) Math.PI / 180F),
                            -Mth.cos(attacker.yRot * (float) Math.PI / 180F));

                    DamageSource damageSource = attacker instanceof Player
                            ? DamageSource.playerAttack((Player) attacker) : DamageSource.indirectMobAttack(attacker, entity);

                    if (trueSweep) {
                        ItemEffectHandler.applyHitEffects(itemStack, entity, attacker);
                        EffectHelper.applyEnchantmentHitEffects(itemStack, entity, attacker);

                        causeTruesweepDamage(damageSource, damage, itemStack, attacker, entity);
                    } else {
                        entity.hurt(damageSource, damage);
                    }

                });

        attacker.level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, attacker.getSoundSource(), 1.0F, 1.0F);

        CastOptional.cast(attacker, Player.class).ifPresent(Player::sweepAttack);
    }

    public static void triggerTruesweep() {
        TetraMod.packetHandler.sendToServer(new TruesweepPacket());
    }

    /**
     * Perfoms a sweeping attack in front of the attacker without requiring a target, dealing damage to nearby entities and playing effects similar to vanilla swords.
     * @param itemStack the itemstack used for the attack
     * @param attacker the attacked entity
     */
    public static void truesweep(ItemStack itemStack, LivingEntity attacker) {
        int sweepingLevel = EffectHelper.getEffectLevel(itemStack, ItemEffect.sweeping);
        float damage = (float) Math.max(attacker.getAttributeValue(Attributes.ATTACK_DAMAGE) * (sweepingLevel * 0.125f), 1);
        float knockback = 0.5f + EnchantmentHelper.getItemEnchantmentLevel(Enchantments.KNOCKBACK, itemStack) * 0.5f;
        double range = 2 + EffectHelper.getEffectEfficiency(itemStack, ItemEffect.sweeping);

        Vec3 target = Vec3.directionFromRotation(attacker.xRot, attacker.yRot)
                .normalize()
                .scale(range)
                .add(attacker.getEyePosition(0));
        AABB aoe = new AABB(target, target);

        // range values set up to mimic vanilla behaviour
        attacker.level.getEntitiesOfClass(LivingEntity.class, aoe.inflate(range, 1d, range)).stream()
                .filter(entity -> entity != attacker)
                .filter(entity -> !attacker.isAlliedTo(entity))
                .forEach(entity -> {
                    entity.knockback(knockback,
                            Mth.sin(attacker.yRot * (float) Math.PI / 180F),
                            -Mth.cos(attacker.yRot * (float) Math.PI / 180F));

                    ItemEffectHandler.applyHitEffects(itemStack, entity, attacker);
                    EffectHelper.applyEnchantmentHitEffects(itemStack, entity, attacker);


                    DamageSource damageSource = attacker instanceof Player
                            ? DamageSource.playerAttack((Player) attacker) : DamageSource.indirectMobAttack(attacker, entity);
                    causeTruesweepDamage(damageSource, damage, itemStack, attacker, entity);
                });

        attacker.level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, attacker.getSoundSource(), 1.0F, 1.0F);

        CastOptional.cast(attacker, Player.class).ifPresent(Player::sweepAttack);
    }

    private static void causeTruesweepDamage(DamageSource damageSource, float baseDamage, ItemStack itemStack, LivingEntity attacker, LivingEntity target) {
        float targetModifier = EnchantmentHelper.getDamageBonus(itemStack, target.getMobType());
        float critMultiplier = CastOptional.cast(attacker, Player.class)
                .map(player -> ForgeHooks.getCriticalHit(player, target, false, 1.5f))
                .map(CriticalHitEvent::getDamageModifier)
                .orElse(1f);

        target.hurt(damageSource, (baseDamage + targetModifier) * critMultiplier);

        if (targetModifier > 0) {
            CastOptional.cast(attacker, Player.class).ifPresent(player -> player.magicCrit(target));
        }

        if (critMultiplier > 1) {
            attacker.getCommandSenderWorld().playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1, 1.3f);
            ((Player) attacker).crit(target);
        }
    }
}
