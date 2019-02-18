package se.mickelus.tetra.items.toolbelt.inventory;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.NBTHelper;
import se.mickelus.tetra.items.toolbelt.ItemToolbeltModular;
import se.mickelus.tetra.items.toolbelt.SlotType;

public class InventoryStorage extends InventoryToolbelt {

    private static final String inventoryKey = "storageInventory";
    public static int maxSize = 24;

    public InventoryStorage(ItemStack stack) {
        super(inventoryKey, stack, maxSize, SlotType.storage);
        ItemToolbeltModular item = (ItemToolbeltModular) stack.getItem();
        numSlots = item.getNumSlots(stack, SlotType.storage);

        readFromNBT(NBTHelper.getTag(stack));
    }
}
