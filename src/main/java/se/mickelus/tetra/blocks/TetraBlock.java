package se.mickelus.tetra.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import se.mickelus.mutil.util.TileEntityOptional;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TetraBlock extends Block implements InitializableBlock {

    public TetraBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static void dropBlockInventory(Block thisBlock, Level world, BlockPos pos, BlockState newState) {
        if (!thisBlock.equals(newState.getBlock())) {
            TileEntityOptional.from(world, pos, ItemHandlerBlockEntity.class)
                    .map(te -> te.getItemHandler(null))
                    .ifPresent(cap -> {
                        for (int i = 0; i < cap.getSlots(); i++) {
                            ItemStack itemStack = cap.getStackInSlot(i);
                            if (!itemStack.isEmpty()) {
                                Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), itemStack.copy());
                            }
                        }
                    });

            TileEntityOptional.from(world, pos, net.minecraft.world.level.block.entity.BlockEntity.class)
                    .ifPresent(net.minecraft.world.level.block.entity.BlockEntity::setRemoved);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> getTicker(BlockEntityType<A> givenType,
            BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
        return expectedType == givenType ? (BlockEntityTicker<A>) ticker : null;
    }
}
