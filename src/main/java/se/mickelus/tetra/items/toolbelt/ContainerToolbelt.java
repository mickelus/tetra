package se.mickelus.tetra.items.toolbelt;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;


public class ContainerToolbelt extends Container {
    private ItemStack itemStackToolbelt;
    private InventoryToolbelt toolbeltInventory;

    public ContainerToolbelt(IInventory playerInventory, ItemStack itemStackToolbelt, EntityPlayer player) {
        this.toolbeltInventory = new InventoryToolbelt(itemStackToolbelt);
        this.itemStackToolbelt = itemStackToolbelt;
        toolbeltInventory.openInventory(player);

        this.addSlotToContainer(new Slot(toolbeltInventory, 0, 79, 18));
        this.addSlotToContainer(new Slot(toolbeltInventory, 1, 94, 33));
        this.addSlotToContainer(new Slot(toolbeltInventory, 2, 65, 33));
        this.addSlotToContainer(new Slot(toolbeltInventory, 3, 79, 47));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(playerInventory, i * 9 + j + 9, j * 17 + 12, i * 17 + 87));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(playerInventory, i, i * 17 + 12, 142));
        }
    }

    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return this.toolbeltInventory.isUseableByPlayer(playerIn);
    }

    /**
     * Take a stack from the specified inventory slot.
     */
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack copy = null;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemStack = slot.getStack();
            copy = itemStack.copy();

            if (index < this.toolbeltInventory.getSizeInventory()) {
                if (!this.mergeItemStack(itemStack,  this.toolbeltInventory.getSizeInventory(), this.inventorySlots.size(), true)) {
                    return null;
                }
            } else if (!this.mergeItemStack(itemStack, 0,  this.toolbeltInventory.getSizeInventory(), false)) {
                return null;
            }

            if (itemStack.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }
        }

        return copy;
    }

    /**
     * Called when the container is closed.
     */
    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);
        this.toolbeltInventory.closeInventory(playerIn);
    }

    public IInventory getToolbeltInventory()
    {
        return this.toolbeltInventory;
    }
}
