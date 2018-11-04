package se.mickelus.tetra.module.schema;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.items.ItemModular;

public abstract class BaseSchema implements UpgradeSchema {

    @Override
    public boolean canApplyUpgrade(EntityPlayer player, ItemStack itemStack, ItemStack[] materials, String slot, int[] availableCapabilities) {
        return isMaterialsValid(itemStack, materials)
                && !isIntegrityViolation(player, itemStack, materials, slot)
                && checkCapabilities(itemStack, materials, availableCapabilities);
    }

    @Override
    public boolean isIntegrityViolation(EntityPlayer player, ItemStack itemStack, final ItemStack[] materials, String slot) {
        ItemStack upgradedStack = applyUpgrade(itemStack, materials, false, slot, null);
        return ItemModular.getIntegrityGain(upgradedStack) + ItemModular.getIntegrityCost(upgradedStack) < 0;
    }

    @Override
    public boolean checkCapabilities(final ItemStack targetStack, final ItemStack[] materials, int[] availableCapabilities) {
        return getRequiredCapabilities(targetStack, materials).stream()
                .allMatch(capability -> availableCapabilities[capability.ordinal()] >= getRequiredCapabilityLevel(targetStack, materials, capability));
    }
}
