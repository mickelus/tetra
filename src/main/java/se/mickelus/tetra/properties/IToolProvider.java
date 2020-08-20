package se.mickelus.tetra.properties;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.module.data.ToolData;

import java.util.Map;
import java.util.Set;

public interface IToolProvider {
    public int getToolLevel(ItemStack itemStack, ToolType tool);
    public float getToolEfficiency(ItemStack itemStack, ToolType tool);
    public Set<ToolType> getTools(ItemStack itemStack);
    public Map<ToolType, Integer> getToolLevels(ItemStack itemStack);

    /**
     * Apply special effects and possibly consume required resources after this item has been used to craft or upgrade
     * another item. This is called once for each tool used by the craft, which this item provide.
     * @param providerStack The providing stack, the itemstack for this item
     * @param targetStack The itemstack which is being upgraded/crafted/altered in some way
     * @param player The player performing the actions
     * @param tool The tool used
     * @param toolLevel The level of the used tool
     * @param consumeResources
     */
    public default ItemStack onCraftConsume(ItemStack providerStack, ItemStack targetStack, PlayerEntity player, ToolType tool, int toolLevel,
            boolean consumeResources) {
        ItemStack result = targetStack.copy();

        return result;
    }

    /**
     * Apply special effects and possibly consume required resources after this item has been used to perform a
     * workbench action.
     * @param providerStack The providing stack, the itemstack for this item
     * @param targetStack The itemstack which the action is performed upon
     * @param player The player performing the action
     * @param tool The tool used
     * @param toolLevel The level of the used tool
     * @param consumeResources
     */
    public default ItemStack onActionConsume(ItemStack providerStack, ItemStack targetStack, PlayerEntity player, ToolType tool, int toolLevel,
            boolean consumeResources) {
        ItemStack result = targetStack.copy();

        return result;
    }
}
