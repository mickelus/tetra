package se.mickelus.tetra.items.toolbelt;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.DisabledSlot;
import se.mickelus.tetra.items.toolbelt.inventory.*;


public class ContainerToolbelt extends Container {
    private ItemStack itemStackToolbelt;
    private InventoryQuickslot quickslotInventory;
    private InventoryStorage storageInventory;
    private InventoryPotions potionsInventory;

    public ContainerToolbelt(IInventory playerInventory, ItemStack itemStackToolbelt, EntityPlayer player) {
        this.quickslotInventory = new InventoryQuickslot(itemStackToolbelt);
        this.storageInventory = new InventoryStorage(itemStackToolbelt);
        this.potionsInventory = new InventoryPotions(itemStackToolbelt);

        this.itemStackToolbelt = itemStackToolbelt;

//        quickslotInventory.openInventory(player);

        int numPotionSlots = potionsInventory.getSizeInventory();
        int numQuickslots = quickslotInventory.getSizeInventory();
        int numStorageSlots = storageInventory.getSizeInventory();
        int offset = 0;

        for (int i = 0; i < numPotionSlots; i++) {
            this.addSlotToContainer(new PotionSlot(potionsInventory, i, (int)(-8.5 * numPotionSlots + 17 * i + 90), 61 - offset * 30));
        }
        if (numPotionSlots > 0) {
            offset++;
        }

        for (int i = 0; i < numQuickslots; i++) {
            this.addSlotToContainer(new Slot(quickslotInventory, i, (int)(-8.5 * numQuickslots + 17 * i + 90), 61 - offset * 30));
        }
        if (numQuickslots > 0) {
            offset++;
        }

        for (int i = 0; i < numStorageSlots; i++) {
            this.addSlotToContainer(new Slot(storageInventory, i, (int)(-8.5 * numStorageSlots + 17 * i + 90), 61 - offset * 30));
        }
        if (numStorageSlots > 0) {
            offset++;
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                Slot slot;
                if (itemStackToolbelt.isItemEqual(playerInventory.getStackInSlot(i * 9 + j + 9))) {
                    slot = new DisabledSlot(playerInventory, i * 9 + j + 9, j * 17 + 12, i * 17 + 116);
                } else {
                    slot = new Slot(playerInventory, i * 9 + j + 9, j * 17 + 12, i * 17 + 116);
                }
                this.addSlotToContainer(slot);
            }
        }

        for (int i = 0; i < 9; i++) {
            Slot slot;
            if (itemStackToolbelt.isItemEqualIgnoreDurability(playerInventory.getStackInSlot(i))) {
                slot = new DisabledSlot(playerInventory, i, i * 17 + 12, 171);
            } else {
                slot = new Slot(playerInventory, i, i * 17 + 12, 171);
            }
            this.addSlotToContainer(slot);
        }
    }

    /**
     * Attempts to merge the given stack into the slots between the given indexes, prioritizing slot stack limits
     * over item stack limits.
     * @param incomingStack an item stack
     * @param startIndex an integer
     * @param endIndex an integer, preferrably larger than startIndex
     * @return true if the given itemstack has been emptied, otherwise false
     */
    private boolean mergeItemStackExtended(ItemStack incomingStack, int startIndex, int endIndex) {

        for (int i = startIndex; i < endIndex; i++) {
            Slot slot = this.inventorySlots.get(i);
            if (slot.isItemValid(incomingStack)) {
                ItemStack slotStack = slot.getStack();
                if (ItemStack.areItemStackTagsEqual(slotStack, incomingStack)) {
                    if (slotStack.getCount() + incomingStack.getCount() < slot.getItemStackLimit(slotStack)) {
                        slotStack.grow(incomingStack.getCount());
                        incomingStack.setCount(0);
                        slot.onSlotChanged();
                        return true;
                    } else {
                        int mergeCount = slot.getItemStackLimit(slotStack) - slotStack.getCount();
                        slotStack.grow(mergeCount);
                        incomingStack.shrink(mergeCount);
                        slot.onSlotChanged();
                    }
                }
            }
        }

        for (int i = startIndex; i < endIndex; i++) {
            Slot slot = this.inventorySlots.get(i);
            if (slot.isItemValid(incomingStack)) {
                ItemStack slotStack = slot.getStack();
                if (slotStack.isEmpty()) {
                    slot.putStack(incomingStack.splitStack(slot.getItemStackLimit(slotStack)));

                    if (incomingStack.isEmpty()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {

        if (clickTypeIn == ClickType.PICKUP && 0 < slotId && slotId < potionsInventory.getSizeInventory()) {
            Slot slot = getSlot(slotId);
            if (player.inventory.getItemStack().isEmpty()) {
                player.inventory.setItemStack(slot.decrStackSize(64));
            } else {
                if (slot.isItemValid(player.inventory.getItemStack())) {
                    if (slot.getStack().isEmpty()) {
                        slot.putStack(player.inventory.getItemStack());
                        player.inventory.setItemStack(ItemStack.EMPTY);
                    } else if (ItemStack.areItemStackTagsEqual(slot.getStack(), player.inventory.getItemStack())) {
                        int moveAmount = Math.min(player.inventory.getItemStack().getCount(), slot.getSlotStackLimit() - slot.getStack().getCount());
                        slot.getStack().grow(moveAmount);
                        player.inventory.getItemStack().shrink(moveAmount);
                    } else if (slot.getStack().getCount() <= slot.getStack().getMaxStackSize()) {
                        ItemStack tempStack = slot.getStack();
                        slot.putStack(player.inventory.getItemStack());
                        player.inventory.setItemStack(tempStack);
                    }
                }
            }
            return ItemStack.EMPTY;
        } else {
            return super.slotClick(slotId, dragType, clickTypeIn, player);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return this.quickslotInventory.isUsableByPlayer(playerIn);
    }

    /**
     * Take a stack from the specified inventory slot.
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemStack = slot.getStack();

            if (itemStack.isItemEqual(itemStackToolbelt)) {
                return ItemStack.EMPTY;
            }

            int numQuickslots = quickslotInventory.getSizeInventory();
            int numStorageSlots = storageInventory.getSizeInventory();
            int numPotionSlots = potionsInventory.getSizeInventory();
            int playerInventoryStart = numQuickslots + numStorageSlots + numPotionSlots;

            if (slot.inventory instanceof InventoryToolbelt) {
                if (slot.isHere(potionsInventory, index)) {
                    itemStack = slot.decrStackSize(64);
                }
                if (numPotionSlots > 0 && !this.mergeItemStack(itemStack, playerInventoryStart,  inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (numPotionSlots > 0 && mergeItemStackExtended(itemStack, 0,  numPotionSlots)) {
                    return itemStack;
                }
                if (numQuickslots > 0 && mergeItemStack(itemStack, numPotionSlots,  numPotionSlots + numQuickslots, false)) {
                    return itemStack;
                }
                if (numStorageSlots > 0 && mergeItemStack(itemStack, numPotionSlots + numQuickslots,  playerInventoryStart, false)) {
                    return itemStack;
                }
                return ItemStack.EMPTY;
            }

            return itemStack;
        }

        return ItemStack.EMPTY;
    }

    /**
     * Called when the container is closed.
     */
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        this.quickslotInventory.closeInventory(playerIn);
    }

    public IInventory getQuickslotInventory() {
        return quickslotInventory;
    }

    public IInventory getStorageInventory() {
        return storageInventory;
    }

    public IInventory getPotionInventory() {
        return potionsInventory;
    }
}
