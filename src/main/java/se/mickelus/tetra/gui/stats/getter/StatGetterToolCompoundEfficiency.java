package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.items.modular.ItemModularHandheld;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class StatGetterToolCompoundEfficiency implements IStatGetter {
    private IStatGetter efficiencyGetter;
    private IStatGetter attackSpeedGetter;
    private IStatGetter enchantmentGetter;

    public StatGetterToolCompoundEfficiency(IStatGetter efficiencyGetter, IStatGetter attackSpeedGetter, IStatGetter enchantmentGetter) {
        this.efficiencyGetter = efficiencyGetter;
        this.attackSpeedGetter = attackSpeedGetter;
        this.enchantmentGetter = enchantmentGetter;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack) {
        return efficiencyGetter.getValue(player, itemStack) * ItemModularHandheld.getAttackSpeedHarvestModifier(attackSpeedGetter.getValue(player, itemStack))
                + ItemModularHandheld.getEfficiencyEnchantmentBonus((int) enchantmentGetter.getValue(player, itemStack));
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot) {
        return efficiencyGetter.getValue(player, itemStack, slot) * ItemModularHandheld.getAttackSpeedHarvestModifier(attackSpeedGetter.getValue(player, itemStack))
                + ItemModularHandheld.getEfficiencyEnchantmentBonus((int) enchantmentGetter.getValue(player, itemStack, slot));
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot, String improvement) {
        return efficiencyGetter.getValue(player, itemStack, improvement) * ItemModularHandheld.getAttackSpeedHarvestModifier(attackSpeedGetter.getValue(player, itemStack))
                + ItemModularHandheld.getEfficiencyEnchantmentBonus((int) enchantmentGetter.getValue(player, itemStack, improvement));
    }
}
