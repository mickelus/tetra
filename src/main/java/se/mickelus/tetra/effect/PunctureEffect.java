package se.mickelus.tetra.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import se.mickelus.tetra.effect.potion.BleedingPotionEffect;
import se.mickelus.tetra.effect.potion.PuncturedPotionEffect;
import se.mickelus.tetra.items.modular.ItemModularHandheld;

public class PunctureEffect extends ChargedAbilityEffect {

    public static final PunctureEffect instance = new PunctureEffect();

    PunctureEffect() {
        super(20, 0.5f, 40, 8, ItemEffect.puncture, TargetRequirement.entity, UseAction.SPEAR, "raised");
    }

    @Override
    public void perform(PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack, LivingEntity target, Vector3d hitVec, int chargedTicks) {
        int armor = target.getTotalArmorValue();

        AbilityUseResult result = item.hitEntity(itemStack, attacker, target, 1, 0.2f, 0.2f);

        if (result != AbilityUseResult.fail) {
            boolean isPunctured = target.getActivePotionEffect(PuncturedPotionEffect.instance) != null;
            if (armor < 5 || isPunctured) {
                target.addPotionEffect(new EffectInstance(BleedingPotionEffect.instance, 80, 1, false, false));
            } else {
                int amplifier = item.getEffectLevel(itemStack, ItemEffect.puncture) - 1;
                int duration = (int) (item.getEffectEfficiency(itemStack, ItemEffect.puncture) * 20);
                target.addPotionEffect(new EffectInstance(PuncturedPotionEffect.instance, duration, amplifier, false, false));
            }

            target.getEntityWorld().playSound(attacker, target.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1, 0.8f);
        } else {
            target.getEntityWorld().playSound(attacker, target.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, SoundCategory.PLAYERS, 1, 0.8f);
        }


        attacker.addExhaustion(0.05f);
        attacker.swing(hand, false);
        attacker.getCooldownTracker().setCooldown(item, getCooldown(item, itemStack) + armor * 20);

        item.tickProgression(attacker, itemStack, result == AbilityUseResult.fail ? 1 : 2);
        item.applyDamage(2, itemStack, attacker);
    }
}
