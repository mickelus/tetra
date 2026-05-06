package se.mickelus.tetra.items.modular.impl.toolbelt.inventory;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.items.modular.impl.toolbelt.ModularToolbeltItem;
import se.mickelus.tetra.items.modular.impl.toolbelt.SlotType;
import se.mickelus.tetra.util.ItemStackTagHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;

@ParametersAreNonnullByDefault
public class QuiverInventory extends ToolbeltInventory {

    private static final String inventoryKey = "quiverInventory";
    public static int maxSize = 30; // 27;

    public QuiverInventory(ItemStack stack, HolderLookup.Provider registryAccess) {
        super(inventoryKey, stack, maxSize, SlotType.quiver, registryAccess);
        ModularToolbeltItem item = (ModularToolbeltItem) stack.getItem();
        numSlots = item.getNumSlots(stack, SlotType.quiver);

        predicate = getPredicate("quiver");

        readFromNBT(ItemStackTagHelper.getOrCreateTag(stack));
    }

    // todo 1.20 verify: toolbelt quiver inventories aggregate stacks in quick access
    // todo 1.20 verify: arrows stacks properly in regular quiver inventory view
    // todo 1.20 verify: drawing from the quiver works (manually by default, automatically with clear offhand and quick latch upgrade)

    /**
     * Returns the number of unique items in this inventory.
     *
     * @return
     */
    public ItemStack[] getAggregatedStacks() {
        ArrayList<ItemStack> aggregatedStacks = new ArrayList<>();
        for (ItemStack itemStack : inventoryContents) {
            boolean found = false;
            for (ItemStack aggregatedStack : aggregatedStacks) {
                if (ItemStack.isSameItemSameComponents(itemStack, aggregatedStack)) {
                    found = true;
                    aggregatedStack.grow(itemStack.getCount());
                    break;
                }
            }
            if (!found && !itemStack.isEmpty()) {
                aggregatedStacks.add(itemStack.copy());
            }
        }

        return aggregatedStacks.toArray(new ItemStack[aggregatedStacks.size()]);
    }

    public int getFirstIndexForStack(ItemStack itemStack) {
        for (int i = 0; i < inventoryContents.size(); i++) {
            if (ItemStack.isSameItemSameComponents(itemStack, inventoryContents.get(i))) {
                return i;
            }
        }
        return -1;
    }
}
