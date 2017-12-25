package se.mickelus.tetra.items.toolbelt;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.DisabledSlot;


public class ContainerToolbelt extends Container {
    private ItemStack itemStackToolbelt;
    private InventoryToolbelt toolbeltInventory;

    public ContainerToolbelt(IInventory playerInventory, ItemStack itemStackToolbelt, EntityPlayer player) {
        this.toolbeltInventory = new InventoryToolbelt(itemStackToolbelt);
        this.itemStackToolbelt = itemStackToolbelt;
        toolbeltInventory.openInventory(player);

        int numSlots = toolbeltInventory.getSizeInventory();

        for (int i = 0; i < numSlots; i++) {
            this.addSlotToContainer(new Slot(toolbeltInventory, i, -8 * numSlots + 17 * i + 85, 19));
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                Slot slot;
                if (itemStackToolbelt.isItemEqual(playerInventory.getStackInSlot(i * 9 + j + 9))) {
                    slot = new DisabledSlot(playerInventory, i * 9 + j + 9, j * 17 + 12, i * 17 + 87);
                } else {
                    slot = new Slot(playerInventory, i * 9 + j + 9, j * 17 + 12, i * 17 + 87);
                }
                this.addSlotToContainer(slot);
            }
        }

        for (int i = 0; i < 9; i++) {
            Slot slot;
            if (itemStackToolbelt.isItemEqualIgnoreDurability(playerInventory.getStackInSlot(i))) {
                slot = new DisabledSlot(playerInventory, i, i * 17 + 12, 142);
            } else {
                slot = new Slot(playerInventory, i, i * 17 + 12, 142);
            }
            this.addSlotToContainer(slot);
        }
    }

    public boolean canInteractWith(EntityPlayer playerIn) {
        return this.toolbeltInventory.isUsableByPlayer(playerIn);
    }

    /**
     * Take a stack from the specified inventory slot.
     */
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemStack = slot.getStack();

            if (itemStack.isItemEqual(this.itemStackToolbelt)) {
                return null;
            }

            if (index < this.toolbeltInventory.getSizeInventory()) {
                if (!this.mergeItemStack(itemStack,  this.toolbeltInventory.getSizeInventory(), this.inventorySlots.size(), true)) {
                    return null;
                }
            } else if (!this.mergeItemStack(itemStack, 0,  this.toolbeltInventory.getSizeInventory(), false)) {
                return null;
            }

            if (itemStack.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            return itemStack.copy();
        }

        return null;
    }

    /**
     * Called when the container is closed.
     */
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        this.toolbeltInventory.closeInventory(playerIn);
    }

    public IInventory getToolbeltInventory()
    {
        return this.toolbeltInventory;
    }
}
