package se.mickelus.tetra.blocks.workbench;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.DisabledSlot;
import se.mickelus.tetra.items.toolbelt.InventoryToolbelt;


public class ContainerWorkbench extends Container {
    private TileEntityWorkbench workbench;

    public ContainerWorkbench(IInventory playerInventory, TileEntityWorkbench workbench, EntityPlayer player) {
        this.workbench = workbench;

	    workbench.openInventory(player);

        this.addSlotToContainer(new Slot(workbench, 0, 79, 18));

        // player inventory
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 3; y++) {
                this.addSlotToContainer(new Slot(playerInventory, y * 9 + x + 9, x * 17 + 12, y * 17 + 87));
            }
        }

        // player toolbar
        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(playerInventory, i, i * 17 + 12, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return this.workbench.isUsableByPlayer(playerIn);
    }

    /**
     * Take a stack from the specified inventory slot.
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemStack = slot.getStack();

            if (index < this.workbench.getSizeInventory()) {
                if (!this.mergeItemStack(itemStack,  this.workbench.getSizeInventory(), this.inventorySlots.size(), true)) {
                    return null;
                }
            } else if (!this.mergeItemStack(itemStack, 0,  this.workbench.getSizeInventory(), false)) {
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
    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        this.workbench.closeInventory(playerIn);
    }
}
