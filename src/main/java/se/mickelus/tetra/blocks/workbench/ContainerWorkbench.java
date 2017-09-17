package se.mickelus.tetra.blocks.workbench;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.ToggleableSlot;
import se.mickelus.tetra.module.UpgradeSchema;


public class ContainerWorkbench extends Container {
    private TileEntityWorkbench workbench;

    private ToggleableSlot[] materialSlots;

    public ContainerWorkbench(IInventory playerInventory, TileEntityWorkbench workbench, EntityPlayer player) {
        this.workbench = workbench;

//        materialSlots = new ToggleableSlot[3];

//        workbench.addChangeListener(this::updateMaterialSlots);
        workbench.openInventory(player);

        this.addSlotToContainer(new Slot(workbench, 0, 152, 58));

        // material inventory
        for (int i = 0; i < 3; i++) {
//            materialSlots[i] = new ToggleableSlot(workbench, i + 1, 271, 18 * i + 100);
//            materialSlots[i].toggle(false);
//            this.addSlotToContainer(materialSlots[i]);
            this.addSlotToContainer(new Slot(workbench, i + 1, 271, 18 * i + 105));
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

    private void updateMaterialSlots() {
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
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);

//        workbench.removeChangeListener(this::updateMaterialSlots);
        workbench.closeInventory(playerIn);
    }
}
