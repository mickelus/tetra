package se.mickelus.tetra.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ItemHandlerStream {
    public static Stream<ItemStack> of(BlockGetter world, BlockPos pos) {
        return of(world.getBlockEntity(pos));
    }

    public static Stream<ItemStack> of(BlockEntity tileEntity) {
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
