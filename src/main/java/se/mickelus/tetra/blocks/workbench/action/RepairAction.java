package se.mickelus.tetra.blocks.workbench.action;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.RepairSchematic;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class RepairAction implements WorkbenchAction {

    public static final String key = "repair_action";

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public boolean canPerformOn(PlayerEntity player, ItemStack itemStack) {
        if (itemStack.getItem() instanceof ModularItem) {
            UpgradeSchematic[] schematics = SchematicRegistry.getAvailableSchematics(player, itemStack);
            return Arrays.stream(schematics)
                    .filter(upgradeSchematic -> upgradeSchematic.isApplicableForSlot(null, itemStack))
                    .anyMatch(upgradeSchematic -> upgradeSchematic instanceof RepairSchematic);
        }

        return false;
    }

    @Override
    public Collection<ToolType> getRequiredToolTypes(ItemStack itemStack) {
        return Collections.emptySet();
    }

    @Override
    public int getRequiredToolLevel(ItemStack itemStack, ToolType toolType) {
        return 0;
    }

    @Override
    public Map<ToolType, Integer> getRequiredTools(ItemStack itemStack) {
        return Collections.emptyMap();
    }

    @Override
    public void perform(PlayerEntity player, ItemStack itemStack, WorkbenchTile workbench) {
        UpgradeSchematic[] schematics = SchematicRegistry.getAvailableSchematics(player, itemStack);
        Arrays.stream(schematics)
                .filter(upgradeSchematic -> upgradeSchematic.isApplicableForSlot(null, itemStack))
                .filter(upgradeSchematic -> upgradeSchematic instanceof RepairSchematic)
                .findFirst()
                .map(upgradeSchematic -> (RepairSchematic) upgradeSchematic)
                .ifPresent(repairSchematic -> workbench.setCurrentSchematic(repairSchematic, repairSchematic.getSlot(itemStack)));
    }
}
