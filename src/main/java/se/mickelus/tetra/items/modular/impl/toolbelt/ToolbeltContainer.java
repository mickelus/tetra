package se.mickelus.tetra.items.modular.impl.toolbelt;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.mgui.gui.DisabledSlot;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.*;


public class ToolbeltContainer extends Container {
    private ItemStack itemStackToolbelt;
    private QuickslotInventory quickslotInventory;
    private StorageInventory storageInventory;
    private PotionsInventory potionsInventory;
    private QuiverInventory quiverInventory;

    public ToolbeltContainer(int windowId, IInventory playerInventory, ItemStack itemStackToolbelt, PlayerEntity player) {
        super(ModularToolbeltItem.containerType, windowId);
        this.quickslotInventory = new QuickslotInventory(itemStackToolbelt);
        this.storageInventory = new StorageInventory(itemStackToolbelt);
        this.potionsInventory = new PotionsInventory(itemStackToolbelt);
        this.quiverInventory = new QuiverInventory(itemStackToolbelt);

        this.itemStackToolbelt = itemStackToolbelt;

//        quickslotInventory.openInventory(player);

        int numPotionSlots = potionsInventory.getSizeInventory();
        int numQuickslots = quickslotInventory.getSizeInventory();
        int numStorageSlots = storageInventory.getSizeInventory();
        int numQuiverSlots = quiverInventory.getSizeInventory();
        int offset = 0;

        if (numStorageSlots > 0) {
            int cols = Math.min(numStorageSlots, StorageInventory.getColumns(numStorageSlots));
            int rows = 1 + (numStorageSlots - 1) / cols;

            for (int i = 0; i < numStorageSlots; i++) {
                addSlot(new PredicateSlot(storageInventory, i, (int)(-8.5 * cols + 17 * (i % cols) + 90), 108 - offset - (i / cols) * 17, storageInventory::isItemValid));
            }
            offset += rows * 17 + 13;
        }

        for (int i = 0; i < numQuiverSlots; i++) {
            addSlot(new PredicateSlot(quiverInventory, i, (int)(-8.5 * numQuiverSlots + 17 * i + 90), 108 - offset, quiverInventory::isItemValid));
        }
        if (numQuiverSlots > 0) {
            offset += 30;
        }

        for (int i = 0; i < numPotionSlots; i++) {
            addSlot(new PotionSlot(potionsInventory, i, (int)(-8.5 * numPotionSlots + 17 * i + 90), 108 - offset));
        }
        if (numPotionSlots > 0) {
            offset += 30;
        }

        for (int i = 0; i < numQuickslots; i++) {
            addSlot(new PredicateSlot(quickslotInventory, i, (int)(-8.5 * numQuickslots + 17 * i + 90), 108 - offset, quickslotInventory::isItemValid));
        }
        if (numQuickslots > 0) {
            offset += 30;
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                Slot slot;
                if (itemStackToolbelt.isItemEqual(playerInventory.getStackInSlot(i * 9 + j + 9))) {
                    slot = new DisabledSlot(playerInventory, i * 9 + j + 9, j * 17 + 12, i * 17 + 142);
                } else {
                    slot = new Slot(playerInventory, i * 9 + j + 9, j * 17 + 12, i * 17 + 142);
                }
                addSlot(slot);
            }
        }

        for (int i = 0; i < 9; i++) {
            Slot slot;
            if (itemStackToolbelt.isItemEqualIgnoreDurability(playerInventory.getStackInSlot(i))) {
                slot = new DisabledSlot(playerInventory, i, i * 17 + 12, 197);
            } else {
                slot = new Slot(playerInventory, i, i * 17 + 12, 197);
            }
            addSlot(slot);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static ToolbeltContainer create(int windowId, PlayerInventory inv) {
        ItemStack itemStack = inv.player.getHeldItemMainhand();
        if (!ModularToolbeltItem.instance.equals(itemStack.getItem())) {
            itemStack = inv.player.getHeldItemOffhand();
        }

        if (!ModularToolbeltItem.instance.equals(itemStack.getItem())) {
            itemStack = ToolbeltHelper.findToolbelt(inv.player);
        }

        return new ToolbeltContainer(windowId, inv, itemStack, inv.player);
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
                if (ItemStack.areItemsEqual(slotStack, incomingStack) && ItemStack.areItemStackTagsEqual(slotStack, incomingStack)) {
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
                    slot.putStack(incomingStack.split(slot.getItemStackLimit(slotStack)));

                    if (incomingStack.isEmpty()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        if (clickTypeIn == ClickType.PICKUP && 0 <= slotId && slotId < potionsInventory.getSizeInventory()) {
            Slot slot = getSlot(slotId);
            if (player.inventory.getItemStack().isEmpty()) {
                player.inventory.setItemStack(slot.decrStackSize(64));
            } else {
                if (slot.isItemValid(player.inventory.getItemStack())) {
                    if (slot.getStack().isEmpty()) {
                        slot.putStack(player.inventory.getItemStack());
                        player.inventory.setItemStack(ItemStack.EMPTY);
                    } else if (ItemStack.areItemsEqual(slot.getStack(), player.inventory.getItemStack())
                            && ItemStack.areItemStackTagsEqual(slot.getStack(), player.inventory.getItemStack())) {
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
    public boolean canInteractWith(PlayerEntity playerIn) {
        return this.quickslotInventory.isUsableByPlayer(playerIn);
    }

    /**
     * Take a stack from the specified inventory slot.
     */
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        Slot slot = inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemStack = slot.getStack();

            if (itemStack.isItemEqual(itemStackToolbelt)) {
                return ItemStack.EMPTY;
            }

            int numQuiverSlots = quiverInventory.getSizeInventory();
            int numPotionSlots = potionsInventory.getSizeInventory();
            int numQuickslots = quickslotInventory.getSizeInventory();
            int numStorageSlots = storageInventory.getSizeInventory();
            int playerInventoryStart = numQuickslots + numStorageSlots + numPotionSlots + numQuiverSlots;

            if (slot.inventory instanceof ToolbeltInventory) {
                // handle moving from potion slots separately
                if (slot.inventory == potionsInventory && slot.getSlotIndex() == index) {
                    int count = slot.getStack().getCount();
                    itemStack = slot.decrStackSize(64);
                    if (!this.mergeItemStack(itemStack, playerInventoryStart,  inventorySlots.size(), true)) {
                        // reset count if it was not possible to move the itemstack
                        itemStack.setCount(count);
                        slot.putStack(itemStack);
                        return ItemStack.EMPTY;
                    }
                } else {
                    // move item from slot into player inventory
                    if (!this.mergeItemStack(itemStack, playerInventoryStart,  inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                }

                slot.onSlotChanged();
            } else {
                if (numPotionSlots > 0 && mergeItemStackExtended(itemStack, 0,  numPotionSlots)) {
                    return itemStack;
                }
                if (numQuiverSlots > 0 && mergeItemStack(itemStack, numPotionSlots,  numPotionSlots + numQuiverSlots, false)) {
                    return itemStack;
                }
                if (numQuickslots > 0 && mergeItemStack(itemStack, numPotionSlots + numQuiverSlots,  numPotionSlots + numQuiverSlots + numQuickslots, false)) {
                    return itemStack;
                }
                if (numStorageSlots > 0 && mergeItemStack(itemStack, numPotionSlots + numQuiverSlots + numQuickslots,  playerInventoryStart, false)) {
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
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);
        this.quickslotInventory.closeInventory(playerIn);
    }

    public QuickslotInventory getQuickslotInventory() {
        return quickslotInventory;
    }

    public StorageInventory getStorageInventory() {
        return storageInventory;
    }

    public PotionsInventory getPotionInventory() {
        return potionsInventory;
    }

    public QuiverInventory getQuiverInventory() {
        return quiverInventory;
    }
}
