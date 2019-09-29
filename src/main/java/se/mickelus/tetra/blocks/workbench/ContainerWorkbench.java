package se.mickelus.tetra.blocks.workbench;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.ToggleableSlot;
import se.mickelus.tetra.module.schema.UpgradeSchema;


public class ContainerWorkbench extends Container {
    private TileEntityWorkbench workbench;

    private ToggleableSlot[] materialSlots;

    public ContainerWorkbench(IInventory playerInventory, TileEntityWorkbench workbench, PlayerEntity player) {
        this.workbench = workbench;

        materialSlots = new ToggleableSlot[3];

        workbench.openInventory(player);

        this.addSlotToContainer(new Slot(workbench, 0, 152, 58));

        // material inventory
        for (int i = 0; i < 3; i++) {
            materialSlots[i] = new ToggleableSlot(workbench, i + 1, 167, 107 + 18 * i);
            materialSlots[i].toggle(false);
            this.addSlotToContainer(materialSlots[i]);
        }

        // player inventory
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 3; y++) {
                this.addSlotToContainer(new Slot(playerInventory, y * 9 + x + 9, x * 17 + 84, y * 17 + 166));
            }
        }

        // player toolbar
        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(playerInventory, i, i * 17 + 84, 221));
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return this.workbench.isUsableByPlayer(playerIn);
    }

    /**
     * Take a stack from the specified inventory slot.
     */
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemStack = slot.getStack();

            if (index < this.workbench.getSizeInventory()) {
                if (!this.mergeItemStack(itemStack,  this.workbench.getSizeInventory(), this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemStack, 0,  this.workbench.getSizeInventory(), false)) {
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

    public void updateSlots() {
        UpgradeSchema currentSchema = workbench.getCurrentSchema();
        int numMaterialSlots = 0;

        if (currentSchema != null) {
            numMaterialSlots = currentSchema.getNumMaterialSlots();
        }

        for (int i = 0; i < numMaterialSlots; i++) {
            materialSlots[i].toggle(true);
        }

        for (int i = numMaterialSlots; i < materialSlots.length; i++) {
            materialSlots[i].toggle(false);
        }
    }

    /**
     * Called when the container is closed.
     */
    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);

        workbench.closeInventory(playerIn);
    }
}
