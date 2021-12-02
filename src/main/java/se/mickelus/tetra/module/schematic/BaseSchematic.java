package se.mickelus.tetra.module.schematic;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.util.CastOptional;

import java.util.Map;

public abstract class BaseSchematic implements UpgradeSchematic {
    @Override
    public boolean canApplyUpgrade(Player player, ItemStack itemStack, ItemStack[] materials, String slot, Map<ToolAction, Integer> availableTools) {
        return isMaterialsValid(itemStack, slot, materials)
                && !isIntegrityViolation(player, itemStack, materials, slot)
                && checkTools(itemStack, materials, availableTools)
                && (player.isCreative() || player.experienceLevel >= getExperienceCost(itemStack, materials, slot));
    }

    @Override
    public boolean isIntegrityViolation(Player player, ItemStack itemStack, final ItemStack[] materials, String slot) {
        ItemStack upgradedStack = applyUpgrade(itemStack, materials, false, slot, null);
        return CastOptional.cast(upgradedStack.getItem(), IModularItem.class)
                .map(item -> item.getProperties(upgradedStack))
                .map(properties -> properties.integrity < properties.integrityUsage)
                .orElse(true);
    }

    @Override
    public boolean checkTools(final ItemStack targetStack, final ItemStack[] materials, Map<ToolAction, Integer> availableTools) {
        return getRequiredToolLevels(targetStack, materials).entrySet().stream()
                .allMatch(entry -> availableTools.getOrDefault(entry.getKey(), 0) >= entry.getValue());
    }

    @Override
    public OutcomePreview[] getPreviews(ItemStack targetStack, String slot) {
        return new OutcomePreview[0];
    }
}
