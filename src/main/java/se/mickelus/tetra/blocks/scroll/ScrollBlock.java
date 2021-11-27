package se.mickelus.tetra.blocks.scroll;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.*;
import net.minecraftforge.fml.network.NetworkHooks;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.workbench.AbstractWorkbenchBlock;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.util.CastOptional;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;

public class ScrollBlock extends TetraBlock {

    private Arrangement arrangement;

    public static final Material material = new Material.Builder(MaterialColor.WOOL).notSolid().build();
    public static final SoundType sound = new SoundType(0.8F, 1.3F, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundEvents.ITEM_BOOK_PAGE_TURN,
            SoundEvents.ITEM_BOOK_PAGE_TURN, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundEvents.ITEM_BOOK_PAGE_TURN);

    public ScrollBlock(String registryName, Arrangement arrangement) {
        super(Properties.create(material).sound(sound));

        setRegistryName(TetraMod.MOD_ID, registryName);

        this.arrangement = arrangement;
        this.setDefaultState(this.getDefaultState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
    }

    public Arrangement getArrangement() {
        return arrangement;
    }

    @Override
    public boolean canUnlockSchematics(World world, BlockPos pos, BlockPos targetPos) {
        boolean isIntricate = TileEntityOptional.from(world, pos, ScrollTile.class).map(ScrollTile::isIntricate).orElse(false);
        return !isIntricate || targetPos.up().equals(pos);
    }

    @Override
    public ResourceLocation[] getSchematics(World world, BlockPos pos, BlockState blockState) {
        return TileEntityOptional.from(world, pos, ScrollTile.class).map(ScrollTile::getSchematics).orElseGet(() -> new ResourceLocation[0]);
    }

    @Override
    public boolean canUnlockCraftingEffects(World world, BlockPos pos, BlockPos targetPos) {
        boolean isIntricate = TileEntityOptional.from(world, pos, ScrollTile.class).map(ScrollTile::isIntricate).orElse(false);
        return !isIntricate || targetPos.up().equals(pos);
    }

    @Override
    public ResourceLocation[] getCraftingEffects(World world, BlockPos pos, BlockState blockState) {
        return TileEntityOptional.from(world, pos, ScrollTile.class).map(ScrollTile::getCraftingEffects).orElseGet(() -> new ResourceLocation[0]);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (arrangement == Arrangement.open) {
            BlockState offsetState = world.getBlockState(pos.down());

            if (offsetState.getBlock()  instanceof AbstractWorkbenchBlock) {
                return offsetState.onBlockActivated(world, player, hand, new BlockRayTraceResult(Vector3d.ZERO, Direction.UP, pos.down(), true));
            }
        }

        return super.onBlockActivated(state, world, pos, player, hand, hit);
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
        Direction facing = Direction.UP;
        if (getArrangement() == Arrangement.wall) {
            facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        }

        BlockPos offsetPos = pos.offset(facing.getOpposite());
        BlockState offsetState = world.getBlockState(offsetPos);

        if (getArrangement() == Arrangement.open) {
            return offsetState.getBlock() instanceof AbstractWorkbenchBlock;
        }

        return offsetState.isSolidSide(world, offsetPos, facing);
    }

    @Override
    public BlockState updatePostPlacement(BlockState blockState, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos, BlockPos facingPos) {
        if (!blockState.isValidPosition(world, currentPos)) {
            if (!world.isRemote() && world.getWorldInfo().getGameRulesInstance().getBoolean(GameRules.DO_TILE_DROPS) && world instanceof World) {
                dropScrolls((World) world, currentPos);
            }
            return Blocks.AIR.getDefaultState();
        }

        return blockState;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean hasTileEntity(final BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world) {
        return new ScrollTile();
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBlockHarvested(world, pos, state, player);

        if (!world.isRemote && !player.isCreative() && world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
            dropScrolls(world, pos);
        }
    }

    public void dropScrolls(World world, BlockPos pos) {
        TileEntityOptional.from(world, pos, ScrollTile.class)
                .ifPresent(tile -> {
                    for (CompoundNBT nbt: tile.getItemTags()) {
                        ItemStack itemStack = new ItemStack(ScrollItem.instance);
                        itemStack.setTagInfo("BlockEntityTag", nbt);

                        ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, itemStack);
                        entity.setDefaultPickupDelay();
                        world.addEntity(entity);
                    }
                });
    }

    public enum Arrangement {
        wall,
        open,
        rolled
    }
}
