package se.mickelus.tetra.items.toolbelt;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class UtilToolbelt {
    public static void equipItemFromToolbelt(EntityPlayer player, int index, EnumHand hand) {
        InventoryToolbelt toolbeltInventory = findToolbeltInventory(player);

        if (toolbeltInventory == null || toolbeltInventory.getStackInSlot(index).isEmpty()) {
            return;
        }

        ItemStack heldItemStack = player.getHeldItem(hand);

        player.setHeldItem(hand, toolbeltInventory.getStackInSlot(index));
        toolbeltInventory.setInventorySlotContents(index, ItemStack.EMPTY);

        if (!heldItemStack.isEmpty()) {
            storeItemInToolbelt(toolbeltInventory, heldItemStack);
        }
    }

    /**
     * Attempts to store the given players offhand or mainhand item in the toolbelt. Attempts to grab the offhand item
     * first and grabs the mainhand item if the offhand is empty.
     * @param player A player
     * @return false if the toolbelt is full, otherwise true
     */
    public static boolean storeItemInToolbelt(EntityPlayer player) {
        InventoryToolbelt toolbeltInventory = findToolbeltInventory(player);
        ItemStack itemStack = player.getHeldItem(EnumHand.OFF_HAND);
        EnumHand sourceHand = EnumHand.OFF_HAND;

        if (itemStack.isEmpty()) {
            itemStack = player.getHeldItem(EnumHand.MAIN_HAND);
            sourceHand = EnumHand.MAIN_HAND;
        }

        if (itemStack.isEmpty() || itemStack.getItem() == ItemToolbeltModular.instance || toolbeltInventory == null) {
            return true;
        }

        if (storeItemInToolbelt(toolbeltInventory, itemStack)) {
            player.setHeldItem(sourceHand, ItemStack.EMPTY);
            return true;
        }
        return false;
    }

    public static boolean storeItemInToolbelt(InventoryToolbelt toolbeltInventory, ItemStack itemStack) {
        // attempt to merge the itemstack with itemstacks in the toolbelt
        for (int i = 0; i < toolbeltInventory.getSizeInventory(); i++) {
            ItemStack storedStack = toolbeltInventory.getStackInSlot(i);
            if (storedStack.isItemEqual(itemStack)
                    && storedStack.getCount() < storedStack.getMaxStackSize()) {

                int moveCount = Math.min(itemStack.getCount(), storedStack.getMaxStackSize() - storedStack.getCount());
                storedStack.grow(moveCount);
                toolbeltInventory.setInventorySlotContents(i, storedStack);
                itemStack.shrink(moveCount);

                if (itemStack.isEmpty()) {
                    return true;
                }
            }
        }

        // attempt to put the itemstack back in a slot it's been in before
        int restockIndex = getShadowIndex(toolbeltInventory, itemStack);
        if (restockIndex != -1) {
            toolbeltInventory.setInventorySlotContents(restockIndex, itemStack);
            return true;
        }

        // put item in the first empty slot
        for (int i = 0; i < toolbeltInventory.getSizeInventory(); i++) {
            if (toolbeltInventory.getStackInSlot(i).isEmpty()) {
                toolbeltInventory.setInventorySlotContents(i, itemStack);
                return true;
            }
        }
        return false;
    }

    public static InventoryToolbelt findToolbeltInventory(EntityPlayer player) {
        ItemStack itemStack = findToolbelt(player);
        if (!itemStack.isEmpty()) {
            return new InventoryToolbelt(itemStack);
        }
        return null;
    }

    /**
     * Attempts to find the first itemstack containing a toolbelt in the given players inventory.
     * todo: add baubles support
     * @param player A player
     * @return A toolbelt itemstack, or an empty itemstack if the player has to toolbelt
     */
    public static ItemStack findToolbelt(EntityPlayer player) {
        InventoryPlayer inventoryPlayer = player.inventory;
        for (int i = 0; i < inventoryPlayer.mainInventory.size(); ++i) {
            ItemStack itemStack = inventoryPlayer.getStackInSlot(i);
            if (ItemToolbeltModular.instance.equals(itemStack.getItem())) {
                return itemStack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static int getShadowIndex(InventoryToolbelt inventory, ItemStack itemStack) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            if (itemStack.isItemEqual(inventory.getShadowOfSlot(i)) && inventory.getStackInSlot(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    public static void emptyOverflowSlots(ItemStack itemStack, EntityPlayer player) {
        new InventoryToolbelt(itemStack).emptyOverflowSlots(player);
    }
}
