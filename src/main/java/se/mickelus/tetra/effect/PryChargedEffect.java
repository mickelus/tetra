package se.mickelus.tetra.effect;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.effect.revenge.RevengeTracker;
import se.mickelus.tetra.items.modular.ItemModularHandheld;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class PryChargedEffect extends ChargedAbilityEffect {

    public static final PryChargedEffect instance = new PryChargedEffect();

    PryChargedEffect() {
        super(20, 0, 40, 3, ItemEffect.pry, TargetRequirement.entity, UseAnim.SPEAR, "raised");
    }

    @Override
    public boolean isAvailable(ItemModularHandheld item, ItemStack itemStack) {
        return super.isAvailable(item, itemStack) && item.getEffectLevel(itemStack, ItemEffect.abilityOvercharge) > 0;
    }

    @Override
    public void perform(Player attacker, InteractionHand hand, ItemModularHandheld item, ItemStack itemStack, LivingEntity target, Vec3 hitVec, int chargedTicks) {

        if (!target.level.isClientSide) {
            int amplifier = item.getEffectLevel(itemStack, ItemEffect.pry);
            amplifier += (int) (getOverchargeBonus(item, itemStack, chargedTicks) * item.getEffectEfficiency(itemStack, ItemEffect.abilityOvercharge));

            double damageMultiplier = PryEffect.damageMultiplier;
            damageMultiplier += getOverchargeBonus(item, itemStack, chargedTicks) * item.getEffectLevel(itemStack, ItemEffect.abilityOvercharge) / 100d;

            int comboPoints = ComboPoints.get(attacker);
            boolean isSatiated = !attacker.getFoodData().needsFood();

            AbilityUseResult result = PryEffect.performRegular(attacker, item, itemStack, damageMultiplier, amplifier, target, isSatiated, comboPoints);
            item.tickProgression(attacker, itemStack, result == AbilityUseResult.fail ? 1 : 2);

            int echoLevel = item.getEffectLevel(itemStack, ItemEffect.abilityEcho);
            if (echoLevel > 0) {
                PryEffect.performEcho(attacker, item, itemStack, damageMultiplier, amplifier, target, isSatiated, comboPoints);
            }
        }

        attacker.causeFoodExhaustion(1f);
        attacker.swing(hand, false);
        attacker.getCooldowns().addCooldown(item, getCooldown(item, itemStack));

        if (ComboPoints.canSpend(item, itemStack)) {
            ComboPoints.reset(attacker);
        }

        int revengeLevel = item.getEffectLevel(itemStack, ItemEffect.abilityRevenge);
        if (revengeLevel > 0) {
            RevengeTracker.removeEnemy(attacker, target);
        }

        item.applyDamage(2, itemStack, attacker);
    }
}
