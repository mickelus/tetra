package se.mickelus.tetra.blocks.workbench.action;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.RepairSchematic;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
@ParametersAreNonnullByDefault
public class RepairAction implements WorkbenchAction {

    public static final String key = "repair_action";

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public boolean canPerformOn(@Nullable Player player, WorkbenchTile tile, ItemStack itemStack) {
        if (player != null && itemStack.getItem() instanceof IModularItem) {
            UpgradeSchematic[] schematics = SchematicRegistry.getAvailableSchematics(player, tile, itemStack);
            return Arrays.stream(schematics)
                    .filter(upgradeSchematic -> upgradeSchematic.isApplicableForSlot(null, itemStack))
                    .anyMatch(upgradeSchematic -> upgradeSchematic instanceof RepairSchematic);
        }

        return false;
    }

    @Override
    public Collection<ToolAction> getRequiredToolActions(ItemStack itemStack) {
        return Collections.emptySet();
    }

    @Override
    public int getRequiredToolLevel(ItemStack itemStack, ToolAction toolAction) {
        return 0;
    }

    @Override
    public Map<ToolAction, Integer> getRequiredTools(ItemStack itemStack) {
        return Collections.emptyMap();
    }

    @Override
    public void perform(Player player, ItemStack itemStack, WorkbenchTile workbench) {
        UpgradeSchematic[] schematics = SchematicRegistry.getAvailableSchematics(player, workbench, itemStack);
        Arrays.stream(schematics)
                .filter(upgradeSchematic -> upgradeSchematic.isApplicableForSlot(null, itemStack))
                .filter(upgradeSchematic -> upgradeSchematic instanceof RepairSchematic)
                .findFirst()
                .map(upgradeSchematic -> (RepairSchematic) upgradeSchematic)
                .ifPresent(repairSchematic -> workbench.setCurrentSchematic(repairSchematic, repairSchematic.getSlot(itemStack)));
    }
}
