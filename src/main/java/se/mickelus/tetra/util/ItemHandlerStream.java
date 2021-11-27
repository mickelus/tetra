package se.mickelus.tetra.util;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ItemHandlerStream {
    public static Stream<ItemStack> of(IBlockReader world, BlockPos pos) {
        return of(world.getTileEntity(pos));
    }

    public static Stream<ItemStack> of(TileEntity tileEntity) {
        return Optional.ofNullable(tileEntity)
                .map(te -> te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY))
                .orElse(LazyOptional.empty())
                .map(cap -> StreamSupport.stream(new Spliterators.AbstractSpliterator<ItemStack>(cap.getSlots(), Spliterator.NONNULL | Spliterator.SIZED) {
                    int index = 0;

                    public boolean tryAdvance(Consumer<? super ItemStack> consumer) {
                        if (index < cap.getSlots()) {
                            consumer.accept(cap.getStackInSlot(index++));
                            return true;
                        } else {
                            return false;
                        }
                    }
                }, false))
                .orElseGet(Stream::empty);
    }
}
