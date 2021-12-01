package se.mickelus.tetra.util;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class InventoryStream {
    public static Stream<ItemStack> of(Container inventory) {
        return StreamSupport.stream(new Spliterators.AbstractSpliterator<ItemStack>(inventory.getContainerSize(), Spliterator.NONNULL | Spliterator.SIZED) {
            int index = 0;

            public boolean tryAdvance(Consumer<? super ItemStack> consumer) {
                if (index < inventory.getContainerSize()) {
                    consumer.accept(inventory.getItem(index++));
                    return true;
                } else {
                    return false;
                }
            }
        }, false);
    }
}
