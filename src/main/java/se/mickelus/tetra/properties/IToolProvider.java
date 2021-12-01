package se.mickelus.tetra.properties;

import com.google.common.cache.Cache;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.module.data.ToolData;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public interface IToolProvider {
    Logger logger = LogManager.getLogger();

    public boolean canProvideTools(ItemStack itemStack);

    public ToolData getToolData(ItemStack itemStack);


    default int getToolLevel(ItemStack itemStack, ToolType tool) {
        if (!canProvideTools(itemStack)) {
            return -1;
        }

        return getToolData(itemStack).getLevel(tool);
    }

    default float getToolEfficiency(ItemStack itemStack, ToolType tool) {
        if (!canProvideTools(itemStack)) {
            return 0;
        }

        if (getToolLevel(itemStack, tool) <= 0) {
            return 0;
        }

        return getToolData(itemStack).getEfficiency(tool);
    }

    default Set<ToolType> getTools(ItemStack itemStack) {
        if (!canProvideTools(itemStack)) {
            return Collections.emptySet();
        }

        return getToolData(itemStack).getValues();
    }

    default Map<ToolType, Integer> getToolLevels(ItemStack itemStack) {
        if (!canProvideTools(itemStack)) {
            return Collections.emptyMap();
        }

        return getToolData(itemStack).getLevelMap();
    }

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
    public default ItemStack onCraftConsume(ItemStack providerStack, ItemStack targetStack, Player player, ToolType tool, int toolLevel,
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
    public default ItemStack onActionConsume(ItemStack providerStack, ItemStack targetStack, Player player, ToolType tool, int toolLevel,
            boolean consumeResources) {
        ItemStack result = targetStack.copy();

        return result;
    }
}
