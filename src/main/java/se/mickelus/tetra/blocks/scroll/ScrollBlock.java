package se.mickelus.tetra.blocks.scroll;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.workbench.AbstractWorkbenchBlock;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class ScrollBlock extends TetraBlock implements EntityBlock {

    private Arrangement arrangement;

    public static final Material material = new Material.Builder(MaterialColor.WOOL).nonSolid().build();
    public static final SoundType sound = new SoundType(0.8F, 1.3F, SoundEvents.BOOK_PAGE_TURN, SoundEvents.BOOK_PAGE_TURN,
            SoundEvents.BOOK_PAGE_TURN, SoundEvents.BOOK_PAGE_TURN, SoundEvents.BOOK_PAGE_TURN);

    public ScrollBlock(String registryName, Arrangement arrangement) {
        super(Properties.of(material).sound(sound));

        setRegistryName(TetraMod.MOD_ID, registryName);

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

            if (offsetState.getBlock()  instanceof AbstractWorkbenchBlock) {
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
    public BlockState updateShape(BlockState blockState, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
        if (!blockState.canSurvive(world, currentPos)) {
            if (!world.isClientSide() && world.getLevelData().getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && world instanceof Level) {
                dropScrolls((Level) world, currentPos);
            }
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

    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(world, pos, state, player);

        if (!world.isClientSide && !player.isCreative() && world.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            dropScrolls(world, pos);
        }
    }

    public void dropScrolls(Level world, BlockPos pos) {
        TileEntityOptional.from(world, pos, ScrollTile.class)
                .ifPresent(tile -> {
                    for (CompoundTag nbt: tile.getItemTags()) {
                        ItemStack itemStack = new ItemStack(ScrollItem.instance);
                        itemStack.addTagElement("BlockEntityTag", nbt);

                        ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, itemStack);
                        entity.setDefaultPickUpDelay();
                        world.addFreshEntity(entity);
                    }
                });
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new ScrollTile(p_153215_, p_153216_);
    }

    public enum Arrangement {
        wall,
        open,
        rolled
    }
}
