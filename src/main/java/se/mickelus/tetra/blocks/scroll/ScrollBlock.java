package se.mickelus.tetra.blocks.scroll;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import se.mickelus.mutil.util.TileEntityOptional;
import se.mickelus.tetra.blocks.ICraftingEffectProviderBlock;
import se.mickelus.tetra.blocks.ISchematicProviderBlock;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.workbench.AbstractWorkbenchBlock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;

@ParametersAreNonnullByDefault
public class ScrollBlock extends TetraBlock implements EntityBlock, ISchematicProviderBlock, ICraftingEffectProviderBlock {
    public static final ResourceLocation scrollDynamicDropId = new ResourceLocation("tetra:scroll");
    public static final SoundType sound = new SoundType(0.8F, 1.3F, SoundEvents.BOOK_PAGE_TURN, SoundEvents.BOOK_PAGE_TURN,
            SoundEvents.BOOK_PAGE_TURN, SoundEvents.BOOK_PAGE_TURN, SoundEvents.BOOK_PAGE_TURN);
    private final Arrangement arrangement;

    public ScrollBlock(Arrangement arrangement) {
        super(Properties.of().sound(sound).instabreak().pushReaction(PushReaction.DESTROY));

        this.arrangement = arrangement;
        this.registerDefaultState(this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
    }

    public Arrangement getArrangement() {
        return arrangement;
    }

    @Override
    public boolean canUnlockSchematics(Level world, BlockPos pos, BlockPos targetPos) {
        boolean isIntricate = TileEntityOptional.from(world, pos, ScrollTile.class).map(ScrollTile::isIntricate).orElse(false);
        return !isIntricate || targetPos.above().equals(pos);
    }

    @Override
    public ResourceLocation[] getSchematics(Level world, BlockPos pos, BlockState blockState) {
        return TileEntityOptional.from(world, pos, ScrollTile.class).map(ScrollTile::getSchematics).orElseGet(() -> new ResourceLocation[0]);
    }

    @Override
    public boolean canUnlockCraftingEffects(Level world, BlockPos pos, BlockPos targetPos) {
        boolean isIntricate = TileEntityOptional.from(world, pos, ScrollTile.class).map(ScrollTile::isIntricate).orElse(false);
        return !isIntricate || targetPos.above().equals(pos);
    }

    @Override
    public ResourceLocation[] getCraftingEffects(Level world, BlockPos pos, BlockState blockState) {
        return TileEntityOptional.from(world, pos, ScrollTile.class).map(ScrollTile::getCraftingEffects).orElseGet(() -> new ResourceLocation[0]);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (arrangement == Arrangement.open) {
            BlockState offsetState = world.getBlockState(pos.below());

            if (offsetState.getBlock() instanceof AbstractWorkbenchBlock) {
                return offsetState.use(world, player, hand, new BlockHitResult(Vec3.ZERO, Direction.UP, pos.below(), true));
            }
        }

        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        Direction facing = Direction.UP;
        if (getArrangement() == Arrangement.wall) {
            facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }

        BlockPos offsetPos = pos.relative(facing.getOpposite());
        BlockState offsetState = world.getBlockState(offsetPos);

        if (getArrangement() == Arrangement.open) {
            return offsetState.getBlock() instanceof AbstractWorkbenchBlock;
        }

        return offsetState.isFaceSturdy(world, offsetPos, facing);
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos,
            BlockPos facingPos) {
        if (!blockState.canSurvive(world, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }

        return blockState;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public List<ItemStack> getDrops(BlockState blockState, LootParams.Builder lootParams) {
        BlockEntity blockentity = lootParams.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockentity instanceof ScrollTile tile) {
            lootParams.withDynamicDrop(scrollDynamicDropId, (consumer) ->
                    Arrays.stream(tile.getItemTags())
                            .map(nbt -> {
                                ItemStack itemStack = new ItemStack(ScrollItem.instance);
                                itemStack.addTagElement("BlockEntityTag", nbt);
                                return itemStack;
                            })
                            .forEach(consumer)
            );

        }

        return super.getDrops(blockState, lootParams);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ScrollTile(blockPos, blockState);
    }

    public enum Arrangement {
        wall,
        open,
        rolled
    }
}
