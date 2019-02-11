package se.mickelus.tetra.blocks.forged.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;


public class ContainerForgedContainer extends Container {
    private TileEntityForgedContainer tileEntity;

    public ContainerForgedContainer(IInventory playerInventory, TileEntityForgedContainer tileEntity, EntityPlayer player) {
        this.tileEntity = tileEntity;
        tileEntity.openInventory(player);

        // container inventory
        for (int i = 0; i < tileEntity.getSizeInventory(); i++) {
            Slot slot = new Slot(tileEntity, i, 167, 107 + 18 * i);
            addSlotToContainer(slot);
        }

        // player inventory
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 3; y++) {
                addSlotToContainer(new Slot(playerInventory, y * 9 + x + 9, x * 17 + 84, y * 17 + 166));
            }
        }

        // player toolbar
        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new Slot(playerInventory, i, i * 17 + 84, 221));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return tileEntity.isUsableByPlayer(playerIn);
    }

    /**
     * Take a stack from the specified inventory slot.
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemStack = slot.getStack();

            if (index < tileEntity.getSizeInventory()) {
                if (!mergeItemStack(itemStack,  tileEntity.getSizeInventory(), inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(itemStack, 0,  tileEntity.getSizeInventory(), false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
            return itemStack.copy();
        }

        return ItemStack.EMPTY;
    }

    /**
     * Called when the container is closed.
     */
    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);

        tileEntity.closeInventory(playerIn);
    }
}
