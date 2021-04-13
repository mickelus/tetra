package se.mickelus.tetra.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import se.mickelus.tetra.effect.potion.SeveredPotionEffect;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.util.CastOptional;

import java.util.Optional;
import java.util.Random;

public class ExecuteEffect extends ChargedAbilityEffect {

    public static final ExecuteEffect instance = new ExecuteEffect();

    ExecuteEffect() {
        super(20, 0.5f, 40, 8, ItemEffect.execute, TargetRequirement.entity, UseAction.SPEAR, "raised");
    }

    @Override
    public void perform(PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack, LivingEntity target, Vector3d hitVec, int chargedTicks) {
        AbilityUseResult result;
        if (isDefensive(item, itemStack, hand)) {
            result = defensiveExecute(attacker, item, itemStack, target);
        } else {
            result = regularExecute(attacker, item, itemStack, target);
        }

        playEffects(result != AbilityUseResult.fail, attacker, target, hitVec);

        attacker.addExhaustion(0.05f);
        attacker.swing(hand, false);
        attacker.getCooldownTracker().setCooldown(item, getCooldown(item, itemStack));

        item.tickProgression(attacker, itemStack, result == AbilityUseResult.fail ? 1 : 2);
        item.applyDamage(2, itemStack, attacker);
    }

    private AbilityUseResult regularExecute(PlayerEntity attacker, ItemModularHandheld item, ItemStack itemStack, LivingEntity target) {
        long harmfulCount = target.getActivePotionEffects().stream()
                .filter(effect -> effect.getPotion().getEffectType() == EffectType.HARMFUL)
                .mapToInt(EffectInstance::getAmplifier)
                .map(amp -> amp + 1)
                .sum();

        float missingHealth = MathHelper.clamp(1 - target.getHealth() / target.getMaxHealth(), 0, 1);
        double efficiency = item.getEffectEfficiency(itemStack, ItemEffect.execute);

        double damageMultiplier = (1 + missingHealth + harmfulCount * efficiency / 100);

        return item.hitEntity(itemStack, attacker, target, damageMultiplier, 0.2f, 0.2f);
    }

    private AbilityUseResult defensiveExecute(PlayerEntity attacker, ItemModularHandheld item, ItemStack itemStack, LivingEntity target) {
        boolean targetFullHealth = target.getMaxHealth() == target.getHealth();
        double damageMultiplier = item.getEffectLevel(itemStack, ItemEffect.abilityDefensive) / 100f;

        if (targetFullHealth) {
            damageMultiplier += item.getEffectEfficiency(itemStack, ItemEffect.abilityDefensive) / 100f;
        }

        AbilityUseResult result = item.hitEntity(itemStack, attacker, target, damageMultiplier, 0.2f, 0.2f);

        if (result != AbilityUseResult.fail) {
            int amp = Optional.ofNullable(target.getActivePotionEffect(SeveredPotionEffect.instance))
                    .map(EffectInstance::getAmplifier)
                    .orElse(-1);
            amp += targetFullHealth ? 2 : 1;
            amp = Math.min(amp, 2);

            target.addPotionEffect(new EffectInstance(SeveredPotionEffect.instance, 1200, amp, false, false));
        }

        return result;
    }


    private void playEffects(boolean isSuccess, PlayerEntity attacker, LivingEntity target, Vector3d hitVec) {
        if (isSuccess) {
            target.getEntityWorld().playSound(attacker, target.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.PLAYERS, 1, 0.8f);

            Random rand = target.getRNG();
            CastOptional.cast(target.world, ServerWorld.class).ifPresent(world ->
                    world.spawnParticle(new RedstoneParticleData(0.6f, 0, 0, 0.8f),
                            hitVec.x, hitVec.y, hitVec.z, 10,
                            rand.nextGaussian() * 0.3, rand.nextGaussian() * 0.3, rand.nextGaussian() * 0.3, 0.1f));
        } else {
            target.getEntityWorld().playSound(attacker, target.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, SoundCategory.PLAYERS, 1, 0.8f);
        }
    }
}
