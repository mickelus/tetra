package se.mickelus.tetra.items.toolbelt;

import baubles.api.BaublesApi;
import baubles.api.cap.BaublesCapabilities;
import baubles.api.cap.IBaublesItemHandler;
import baubles.api.inv.BaublesInventoryWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.Loader;
import se.mickelus.tetra.IntegrationHelper;
import net.minecraft.util.text.TextComponentTranslation;
import se.mickelus.tetra.items.toolbelt.inventory.*;

public class UtilToolbelt {
    public static void equipItemFromToolbelt(EntityPlayer player, ToolbeltSlotType slotType, int index, EnumHand hand) {
        InventoryToolbelt inventory = null;
        ItemStack toolbeltStack = findToolbelt(player);

        switch (slotType) {
            case quickslot:
                inventory = new InventoryQuickslot(toolbeltStack);
                break;
            case potion:
                inventory = new InventoryPotions(toolbeltStack);
                break;
            case quiver:
                inventory = new InventoryQuiver(toolbeltStack);
                break;
            case storage:
                inventory = new InventoryStorage(toolbeltStack);
                break;
        }

        if (inventory.getSizeInventory() <= index || inventory.getStackInSlot(index).isEmpty()) {
            return;
        }

        ItemStack heldItemStack = player.getHeldItem(hand);
        player.setHeldItem(hand, inventory.takeItemStack(index));


        if (!heldItemStack.isEmpty()) {
            if (!storeItemInToolbelt(toolbeltStack, heldItemStack)) {
                if (!player.inventory.addItemStackToInventory(heldItemStack)) {
                    inventory.storeItemInInventory(player.getHeldItem(hand));
                    player.setHeldItem(hand, heldItemStack);
                    player.sendStatusMessage(new TextComponentTranslation("toolbelt.blocked"), true);
                }
            }
        }
    }

    /**
     * Attempts to store the given players offhand or mainhand item in the toolbelt. Attempts to grab the offhand item
     * first and grabs the mainhand item if the offhand is empty.
     * @param player A player
     * @return false if the toolbelt is full, otherwise true
     */
    public static boolean storeItemInToolbelt(EntityPlayer player) {
        ItemStack toolbeltStack = findToolbelt(player);
        ItemStack itemStack = player.getHeldItem(EnumHand.OFF_HAND);
        EnumHand sourceHand = EnumHand.OFF_HAND;

        if (itemStack.isEmpty()) {
            itemStack = player.getHeldItem(EnumHand.MAIN_HAND);
            sourceHand = EnumHand.MAIN_HAND;
        }

        if (toolbeltStack.isEmpty() || itemStack.isEmpty() || itemStack.getItem() == ItemToolbeltModular.instance) {
            return true;
        }

        if (storeItemInToolbelt(toolbeltStack, itemStack)) {
            player.setHeldItem(sourceHand, ItemStack.EMPTY);
            return true;
        }
        return false;
    }

    public static boolean storeItemInToolbelt(ItemStack toolbeltStack, ItemStack itemStack) {
        if (new InventoryPotions(toolbeltStack).storeItemInInventory(itemStack)) {
            return true;
        }

        if (new InventoryQuiver(toolbeltStack).storeItemInInventory(itemStack)) {
            return true;
        }

        if (new InventoryQuickslot(toolbeltStack).storeItemInInventory(itemStack)) {
            return true;
        }

        if (new InventoryStorage(toolbeltStack).storeItemInInventory(itemStack)) {
            return true;
        }

        return false;
    }

    /**
     * Attempts to find the first itemstack containing a toolbelt in the given players inventory.
     * todo: add baubles support
     * @param player A player
     * @return A toolbelt itemstack, or an empty itemstack if the player has no toolbelt
     */
    public static ItemStack findToolbelt(EntityPlayer player) {
        ItemStack baubleToolbelt = getBaubleToolbelt(player);
        if (!baubleToolbelt.isEmpty()) {
            return baubleToolbelt;
        }

        InventoryPlayer inventoryPlayer = player.inventory;
        for (int i = 0; i < inventoryPlayer.mainInventory.size(); ++i) {
            ItemStack itemStack = inventoryPlayer.getStackInSlot(i);
            if (ItemToolbeltModular.instance.equals(itemStack.getItem())) {
                return itemStack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack getBaubleToolbelt(EntityPlayer player) {
        if (Loader.isModLoaded(IntegrationHelper.baublesModId)) {
            IBaublesItemHandler handler = player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);

            if (handler != null) {
                handler.setPlayer(player);
                IInventory baubleInventory = new BaublesInventoryWrapper(handler, player);

                for (int i = 0; i < baubleInventory.getSizeInventory(); i++) {
                    ItemStack itemStack = baubleInventory.getStackInSlot(i);
                    if (ItemToolbeltModular.instance.equals(itemStack.getItem())) {
                        return itemStack;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static void emptyOverflowSlots(ItemStack itemStack, EntityPlayer player) {
        new InventoryQuickslot(itemStack).emptyOverflowSlots(player);
        new InventoryPotions(itemStack).emptyOverflowSlots(player);
        new InventoryStorage(itemStack).emptyOverflowSlots(player);
        new InventoryQuiver(itemStack).emptyOverflowSlots(player);
    }

    public static void updateBauble(EntityPlayer player) {
        if (Loader.isModLoaded(IntegrationHelper.baublesModId)) {
            int baubleSlot = BaublesApi.isBaubleEquipped(player, ItemToolbeltModular.instance);
            if (baubleSlot != -1) {
                BaublesApi.getBaublesHandler(player).setChanged(baubleSlot, true);
            }
        }
    }
}
