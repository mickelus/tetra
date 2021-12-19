package se.mickelus.tetra.blocks.workbench.action;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

public interface WorkbenchAction {

    String getKey();

    boolean canPerformOn(@Nullable Player player, WorkbenchTile tile, ItemStack itemStack);

    Collection<ToolAction> getRequiredToolActions(ItemStack itemStack);

    int getRequiredToolLevel(ItemStack itemStack, ToolAction toolAction);

    Map<ToolAction, Integer> getRequiredTools(ItemStack itemStack);

    void perform(Player player, ItemStack itemStack, WorkbenchTile workbench);

    default boolean allowInWorldInteraction() {
        return false;
    }
}
