package se.mickelus.tetra.items.toolbelt;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class UtilToolbelt {
    public static void equipToolbeltItemInOffhand(EntityPlayer player, int index) {
        InventoryToolbelt toolbeltInventory = findToolbeltInventory(player);
        ItemStack offhandItem = player.getHeldItemOffhand();

        if (toolbeltInventory == null || toolbeltInventory.getStackInSlot(index) == null) {
            return;
        }

        player.setHeldItem(EnumHand.OFF_HAND, toolbeltInventory.getStackInSlot(index));

        toolbeltInventory.setInventorySlotContents(index, null);

        if (offhandItem != null) {
            int restockIndex = getRestockIndex(toolbeltInventory, offhandItem);
            if (restockIndex != -1) {
                toolbeltInventory.setInventorySlotContents(restockIndex, offhandItem);
            } else {
                player.inventory.addItemStackToInventory(offhandItem);
            }
        }
    }

    public static InventoryToolbelt findToolbeltInventory(EntityPlayer player) {
        ItemStack itemStack = findToolbeltItemStack(player);
        if (itemStack != null) {
            return new InventoryToolbelt(itemStack);
        }
        return null;
    }

    public static ItemStack findToolbeltItemStack(EntityPlayer player) {
        InventoryPlayer inventoryPlayer = player.inventory;
        for (int i = 0; i < inventoryPlayer.mainInventory.length; ++i) {
            if (inventoryPlayer.mainInventory[i] != null
                    && ItemToolbelt.instance.equals(inventoryPlayer.mainInventory[i].getItem())) {
                return inventoryPlayer.mainInventory[i];
            }
        }
        return null;
    }

    public static int getRestockIndex(InventoryToolbelt inventory, ItemStack itemStack) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            if (itemStack.isItemEqual(inventory.getShadowOfSlot(i)) && inventory.getStackInSlot(i) == null) {
                return i;
            }
        }
        return -1;
    }
}
