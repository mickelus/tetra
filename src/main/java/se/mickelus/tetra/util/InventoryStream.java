package se.mickelus.tetra.util;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class InventoryStream {
    public static Stream<ItemStack> of(IInventory inventory) {
        return StreamSupport.stream(new Spliterators.AbstractSpliterator<ItemStack>(inventory.getSizeInventory(), Spliterator.NONNULL | Spliterator.SIZED) {
            int index = 0;

            public boolean tryAdvance(Consumer<? super ItemStack> consumer) {
                if (index < inventory.getSizeInventory()) {
                    consumer.accept(inventory.getStackInSlot(index++));
                    return true;
                } else {
                    return false;
                }
            }
        }, false);
    }
}
