package se.mickelus.tetra.items.modular.impl.toolbelt.inventory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.items.modular.impl.toolbelt.ModularToolbeltItem;
import se.mickelus.tetra.items.modular.impl.toolbelt.SlotType;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
public class PotionsInventory extends ToolbeltInventory {

    private static final String inventoryKey = "potionsInventory";
    public static int maxSize = 10; // 9;

    public PotionsInventory(ItemStack stack) {
        super(inventoryKey, stack, maxSize, SlotType.potion);
        ModularToolbeltItem item = (ModularToolbeltItem) stack.getItem();
        numSlots = item.getNumSlots(stack, SlotType.potion);

        predicate = getPredicate("potion");

        readFromNBT(stack.getOrCreateTag());
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack itemStack) {
        return isItemValid(itemStack);
    }

    @Override
    public boolean storeItemInInventory(ItemStack itemStack) {
        if (!isItemValid(itemStack)) {
            return false;
        }

        // attempt to merge the itemstack with itemstacks in the inventory
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack storedStack = getItem(i);
            if (ItemStack.isSame(storedStack, itemStack)
                    && ItemStack.tagMatches(storedStack, itemStack)
                    && storedStack.getCount() < 64) {

                int moveCount = Math.min(itemStack.getCount(), 64 - storedStack.getCount());
                storedStack.grow(moveCount);
                setItem(i, storedStack);
                itemStack.shrink(moveCount);

                if (itemStack.isEmpty()) {
                    return true;
                }
            }
        }

        // put item in the first empty slot
        for (int i = 0; i < getContainerSize(); i++) {
            if (getItem(i).isEmpty()) {
                setItem(i, itemStack);
                return true;
            }
        }
        return false;
    }

    public ItemStack takeItemStack(int index) {
        ItemStack itemStack = getItem(index);
        itemStack = itemStack.split(itemStack.getMaxStackSize());
        if (getItem(index).isEmpty()) {
            setItem(index, ItemStack.EMPTY);
        } else {
            setChanged();
        }
        return itemStack;
    }

    @Override
    public void emptyOverflowSlots(Player player) {
        for (int i = getContainerSize(); i < maxSize; i++) {
            ItemStack itemStack = getItem(i);
            while (!itemStack.isEmpty()) {
                moveStackToPlayer(itemStack.split(itemStack.getMaxStackSize()), player);
            }
        }
        setChanged();
    }


}
