package se.mickelus.tetra.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import se.mickelus.tetra.effect.potion.BleedingPotionEffect;
import se.mickelus.tetra.effect.potion.ExhaustedPotionEffect;
import se.mickelus.tetra.effect.potion.PriedPotionEffect;
import se.mickelus.tetra.effect.potion.PuncturedPotionEffect;
import se.mickelus.tetra.items.modular.ItemModularHandheld;

import javax.annotation.Nullable;
import java.util.Optional;

public class OverpowerEffect extends ChargedAbilityEffect {

    public static final OverpowerEffect instance = new OverpowerEffect();

    OverpowerEffect() {
        super(10, 1f, 10, 1, ItemEffect.overpower, TargetRequirement.none, UseAction.SPEAR, "raised");
    }


    public void perform(PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack,
            @Nullable LivingEntity target, @Nullable BlockPos targetPos, @Nullable Vector3d hitVec, int chargedTicks) {
        super.perform(attacker, hand, item, itemStack, target, targetPos, hitVec, chargedTicks);

        double efficiency = item.getEffectEfficiency(itemStack, ItemEffect.overpower);

        int attackerAmp = Optional.ofNullable(attacker.getActivePotionEffect(ExhaustedPotionEffect.instance))
                .map(EffectInstance::getAmplifier)
                .orElse(-1);
        attacker.addPotionEffect(new EffectInstance(ExhaustedPotionEffect.instance, (int) (efficiency * 20), attackerAmp + 1, false, true));

        attacker.addExhaustion(0.05f);
        attacker.swing(hand, false);
        attacker.getCooldownTracker().setCooldown(item, getCooldown(item, itemStack));
    }

    @Override
    public void perform(PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack, LivingEntity target, Vector3d hitVec, int chargedTicks) {
        double level = item.getEffectLevel(itemStack, ItemEffect.overpower);
        double efficiency = item.getEffectEfficiency(itemStack, ItemEffect.overpower);

        AbilityUseResult result = item.hitEntity(itemStack, attacker, target, level / 100f, 0.1f, 0.1f);
        if (result != AbilityUseResult.fail) {
            int targetAmp = Optional.ofNullable(target.getActivePotionEffect(ExhaustedPotionEffect.instance))
                    .map(EffectInstance::getAmplifier)
                    .orElse(-1);

            target.addPotionEffect(new EffectInstance(ExhaustedPotionEffect.instance, (int) (efficiency * 20), targetAmp + 2, false, true));

            target.getEntityWorld().playSound(attacker, target.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1, 0.8f);
        } else {
            target.getEntityWorld().playSound(attacker, target.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, SoundCategory.PLAYERS, 1, 0.8f);
        }


        item.tickProgression(attacker, itemStack, result == AbilityUseResult.fail ? 1 : 2);
        item.applyDamage(2, itemStack, attacker);
    }
}
