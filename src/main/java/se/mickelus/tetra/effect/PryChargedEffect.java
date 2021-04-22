package se.mickelus.tetra.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import se.mickelus.tetra.effect.revenge.RevengeTracker;
import se.mickelus.tetra.items.modular.ItemModularHandheld;

public class PryChargedEffect extends ChargedAbilityEffect {

    public static final PryChargedEffect instance = new PryChargedEffect();

    PryChargedEffect() {
        super(20, 0, 40, 3, ItemEffect.pry, TargetRequirement.entity, UseAction.SPEAR, "raised");
    }

    @Override
    public boolean isAvailable(ItemModularHandheld item, ItemStack itemStack) {
        return super.isAvailable(item, itemStack) && item.getEffectLevel(itemStack, ItemEffect.abilityOvercharge) > 0;
    }

    @Override
    public void perform(PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack, LivingEntity target, Vector3d hitVec, int chargedTicks) {

        if (!target.world.isRemote) {
            int amplifier = item.getEffectLevel(itemStack, ItemEffect.pry);
            amplifier += (int) (getOverchargeBonus(item, itemStack, chargedTicks) * item.getEffectEfficiency(itemStack, ItemEffect.abilityOvercharge));

            double damageMultiplier = PryEffect.damageMultiplier;
            damageMultiplier += getOverchargeBonus(item, itemStack, chargedTicks) * item.getEffectLevel(itemStack, ItemEffect.abilityOvercharge) / 100d;

            AbilityUseResult result = PryEffect.performRegular(attacker, item, itemStack, damageMultiplier, amplifier, target);
            item.tickProgression(attacker, itemStack, result == AbilityUseResult.fail ? 1 : 2);
        }

        attacker.addExhaustion(0.05f);
        attacker.swing(hand, false);
        attacker.getCooldownTracker().setCooldown(item, getCooldown(item, itemStack));

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
