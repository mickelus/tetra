package se.mickelus.tetra.items.modular.impl.toolbelt;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.mutil.gui.DisabledSlot;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.*;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ToolbeltContainer extends AbstractContainerMenu {
    private final ItemStack itemStackToolbelt;
    private final QuickslotInventory quickslotInventory;
    private final StorageInventory storageInventory;
    private final PotionsInventory potionsInventory;
    private final QuiverInventory quiverInventory;

    public ToolbeltContainer(int windowId, Container playerInventory, ItemStack itemStackToolbelt, Player player) {
        super(ModularToolbeltItem.containerType, windowId);
        this.quickslotInventory = new QuickslotInventory(itemStackToolbelt);
        this.storageInventory = new StorageInventory(itemStackToolbelt);
        this.potionsInventory = new PotionsInventory(itemStackToolbelt);
        this.quiverInventory = new QuiverInventory(itemStackToolbelt);

        this.itemStackToolbelt = itemStackToolbelt;

        int numPotionSlots = potionsInventory.getContainerSize();
        int numQuickslots = quickslotInventory.getContainerSize();
        int numStorageSlots = storageInventory.getContainerSize();
        int numQuiverSlots = quiverInventory.getContainerSize();
        int offset = 0;

        if (numStorageSlots > 0) {
            int cols = Math.min(numStorageSlots, StorageInventory.getColumns(numStorageSlots));
            int rows = 1 + (numStorageSlots - 1) / cols;

            for (int i = 0; i < numStorageSlots; i++) {
                addSlot(new PredicateSlot(storageInventory, i, (int) (-8.5 * cols + 17 * (i % cols) + 90), 108 - offset - (i / cols) * 17, storageInventory::isItemValid));
            }
            offset += rows * 17 + 13;
        }

        for (int i = 0; i < numQuiverSlots; i++) {
            addSlot(new PredicateSlot(quiverInventory, i, (int) (-8.5 * numQuiverSlots + 17 * i + 90), 108 - offset, quiverInventory::isItemValid));
        }
        if (numQuiverSlots > 0) {
            offset += 30;
        }

        for (int i = 0; i < numPotionSlots; i++) {
            addSlot(new PotionSlot(potionsInventory, i, (int) (-8.5 * numPotionSlots + 17 * i + 90), 108 - offset));
        }
        if (numPotionSlots > 0) {
            offset += 30;
        }

        for (int i = 0; i < numQuickslots; i++) {
            addSlot(new PredicateSlot(quickslotInventory, i, (int) (-8.5 * numQuickslots + 17 * i + 90), 108 - offset, quickslotInventory::isItemValid));
        }
        if (numQuickslots > 0) {
            offset += 30;
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                Slot slot;
                if (itemStackToolbelt.sameItem(playerInventory.getItem(i * 9 + j + 9))) {
                    slot = new DisabledSlot(playerInventory, i * 9 + j + 9, j * 17 + 12, i * 17 + 142);
                } else {
                    slot = new Slot(playerInventory, i * 9 + j + 9, j * 17 + 12, i * 17 + 142);
                }
                addSlot(slot);
            }
        }

        for (int i = 0; i < 9; i++) {
            Slot slot;
            if (itemStackToolbelt.sameItemStackIgnoreDurability(playerInventory.getItem(i))) {
                slot = new DisabledSlot(playerInventory, i, i * 17 + 12, 197);
            } else {
                slot = new Slot(playerInventory, i, i * 17 + 12, 197);
            }
            addSlot(slot);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static ToolbeltContainer create(int windowId, Inventory inv) {
        ItemStack itemStack = inv.player.getMainHandItem();
        if (!ModularToolbeltItem.instance.equals(itemStack.getItem())) {
            itemStack = inv.player.getOffhandItem();
        }

        if (!ModularToolbeltItem.instance.equals(itemStack.getItem())) {
            itemStack = ToolbeltHelper.findToolbelt(inv.player);
        }

        return new ToolbeltContainer(windowId, inv, itemStack, inv.player);
    }

    /**
     * Attempts to merge the given stack into the slots between the given indexes, prioritizing slot stack limits
     * over item stack limits.
     *
     * @param incomingStack an item stack
     * @param startIndex    an integer
     * @param endIndex      an integer, preferrably larger than startIndex
     * @return true if the given itemstack has been emptied, otherwise false
     */
    private boolean mergeItemStackExtended(ItemStack incomingStack, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            Slot slot = this.slots.get(i);
            if (slot.mayPlace(incomingStack)) {
                ItemStack slotStack = slot.getItem();
                if (ItemStack.isSame(slotStack, incomingStack) && ItemStack.tagMatches(slotStack, incomingStack)) {
                    if (slotStack.getCount() + incomingStack.getCount() < slot.getMaxStackSize(slotStack)) {
                        slotStack.grow(incomingStack.getCount());
                        incomingStack.setCount(0);
                        slot.setChanged();
                        return true;
                    } else {
                        int mergeCount = slot.getMaxStackSize(slotStack) - slotStack.getCount();
                        slotStack.grow(mergeCount);
                        incomingStack.shrink(mergeCount);
                        slot.setChanged();
                    }
                }
            }
        }

        for (int i = startIndex; i < endIndex; i++) {
            Slot slot = this.slots.get(i);
            if (slot.mayPlace(incomingStack)) {
                ItemStack slotStack = slot.getItem();
                if (slotStack.isEmpty()) {
                    slot.set(incomingStack.split(slot.getMaxStackSize(slotStack)));

                    if (incomingStack.isEmpty()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }

    /**
     * Take a stack from the specified inventory slot.
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemStack = slot.getItem();

            if (itemStack.sameItem(itemStackToolbelt)) {
                return ItemStack.EMPTY;
            }

            int numQuiverSlots = quiverInventory.getContainerSize();
            int numPotionSlots = potionsInventory.getContainerSize();
            int numQuickslots = quickslotInventory.getContainerSize();
            int numStorageSlots = storageInventory.getContainerSize();
            int playerInventoryStart = numQuickslots + numStorageSlots + numPotionSlots + numQuiverSlots;

            if (slot.container instanceof ToolbeltInventory) {
                // handle moving from potion slots separately
                if (slot.container == potionsInventory && slot.getSlotIndex() == index) {
                    int count = slot.getItem().getCount();
                    itemStack = slot.remove(64);
                    if (!this.moveItemStackTo(itemStack, playerInventoryStart, slots.size(), true)) {
                        // reset count if it was not possible to move the itemstack
                        itemStack.setCount(count);
                        slot.set(itemStack);
                        slot.setChanged();
                        return ItemStack.EMPTY;
                    }
                } else {
                    ItemStack breakoff = itemStack.split(itemStack.getMaxStackSize());
                    // move item from slot into player inventory
                    if (!this.moveItemStackTo(breakoff, playerInventoryStart, slots.size(), true)) {
                        slot.setChanged();
                        return itemStack;
                    } else {
                        itemStack.grow(breakoff.getCount());
                        slot.setChanged();
                        return ItemStack.EMPTY;
                    }
                }

                slot.setChanged();
            } else {
                // plop item into first available slot, in priority: quiver > potion > quickslot > storage
                // todo: cleanup, this is really confusing

                if (numQuiverSlots > 0 && moveItemStackTo(itemStack, numStorageSlots, numStorageSlots + numQuiverSlots, false)) {
                    return itemStack;
                }
                if (numPotionSlots > 0 && mergeItemStackExtended(itemStack, numStorageSlots + numQuiverSlots, numStorageSlots + numQuiverSlots + numPotionSlots)) {
                    return itemStack;
                }
                if (numQuickslots > 0 && moveItemStackTo(itemStack, numStorageSlots + numQuiverSlots + numPotionSlots, playerInventoryStart, false)) {
                    return itemStack;
                }
                if (numStorageSlots > 0 && moveItemStackTo(itemStack, 0, numStorageSlots, false)) {
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
    public void removed(Player playerIn) {
        super.removed(playerIn);
        this.quickslotInventory.stopOpen(playerIn);
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
