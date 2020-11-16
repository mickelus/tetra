package se.mickelus.tetra.blocks.workbench.action;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

public interface WorkbenchAction {

    public String getKey();
    public boolean canPerformOn(@Nullable PlayerEntity player, ItemStack itemStack);
    public Collection<ToolType> getRequiredToolTypes(ItemStack itemStack);
    public int getRequiredToolLevel(ItemStack itemStack, ToolType toolType);
    public Map<ToolType, Integer> getRequiredTools(ItemStack itemStack);
    public void perform(PlayerEntity player, ItemStack itemStack, WorkbenchTile workbench);

    public default boolean allowInWorldInteraction() {
        return false;
    }
}
