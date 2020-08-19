package se.mickelus.tetra.module.schematic;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.items.modular.ModularItem;

import java.util.Map;

public abstract class BaseSchematic implements UpgradeSchematic {
    @Override
    public boolean canApplyUpgrade(PlayerEntity player, ItemStack itemStack, ItemStack[] materials, String slot, Map<ToolType, Integer> availableTools) {
        return isMaterialsValid(itemStack, slot, materials)
                && !isIntegrityViolation(player, itemStack, materials, slot)
                && checkTools(itemStack, materials, availableTools)
                && (player.isCreative() || player.experienceLevel >= getExperienceCost(itemStack, materials, slot));
    }

    @Override
    public boolean isIntegrityViolation(PlayerEntity player, ItemStack itemStack, final ItemStack[] materials, String slot) {
        ItemStack upgradedStack = applyUpgrade(itemStack, materials, false, slot, null);
        return ModularItem.getIntegrityGain(upgradedStack) + ModularItem.getIntegrityCost(upgradedStack) < 0;
    }

    @Override
    public boolean checkTools(final ItemStack targetStack, final ItemStack[] materials, Map<ToolType, Integer> availableTools) {
        return getRequiredToolLevels(targetStack, materials).entrySet().stream()
                .allMatch(entry -> availableTools.getOrDefault(entry.getKey(), 0) >= entry.getValue());
    }

    @Override
    public OutcomePreview[] getPreviews(ItemStack targetStack, String slot) {
        return new OutcomePreview[0];
    }
}
