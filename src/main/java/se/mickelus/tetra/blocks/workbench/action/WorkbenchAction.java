package se.mickelus.tetra.blocks.workbench.action;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

public interface WorkbenchAction {

    public String getKey();
    public boolean canPerformOn(@Nullable Player player, WorkbenchTile tile, ItemStack itemStack);
    public Collection<ToolAction> getRequiredToolActions(ItemStack itemStack);
    public int getRequiredToolLevel(ItemStack itemStack, ToolAction toolAction);
    public Map<ToolAction, Integer> getRequiredTools(ItemStack itemStack);
    public void perform(Player player, ItemStack itemStack, WorkbenchTile workbench);

    public default boolean allowInWorldInteraction() {
        return false;
    }
}
