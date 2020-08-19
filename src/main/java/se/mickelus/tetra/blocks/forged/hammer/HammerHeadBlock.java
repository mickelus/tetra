package se.mickelus.tetra.blocks.forged.hammer;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.blocks.TetraWaterloggedBlock;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static se.mickelus.tetra.blocks.forged.ForgedBlockCommon.locationTooltip;

public class HammerHeadBlock extends TetraWaterloggedBlock {
    public static final String unlocalizedName = "hammer_head";

    public static final VoxelShape shape = makeCuboidShape(2, 14, 2, 14, 16, 14);

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static HammerHeadBlock instance;

    public HammerHeadBlock() {
        super(ForgedBlockCommon.propertiesSolid);

        setRegistryName(unlocalizedName);

        hasItem = false;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void clientInit() {
        ClientRegistry.bindTileEntityRenderer(HammerHeadTile.type, HammerHeadTESR::new);
    }

    @Override
    public void addInformation(final ItemStack stack, @Nullable final IBlockReader world, final List<ITextComponent> tooltip,
            final ITooltipFlag advanced) {
        tooltip.add(locationTooltip);
    }

    @Override
    public boolean canProvideTools(World world, BlockPos pos, BlockPos targetPos) {
        return pos.equals(targetPos.up());
    }

    @Override
    public Collection<ToolType> getTools(World world, BlockPos pos, BlockState blockState) {
        BlockPos basePos = pos.up();
        if (world.getBlockState(basePos).getBlock() instanceof HammerBaseBlock) {
            HammerBaseBlock baseBlock = (HammerBaseBlock) world.getBlockState(basePos).getBlock();

            if (baseBlock.isFueled(world, basePos)) {
                return Collections.singletonList(ToolTypes.hammer);
            }
        }
        return super.getTools(world, pos, blockState);
    }

    @Override
    public int getToolLevel(World world, BlockPos pos, BlockState blockState, ToolType toolType) {
        BlockPos basePos = pos.up();
        if (ToolTypes.hammer.equals(toolType) && world.getBlockState(basePos).getBlock() instanceof HammerBaseBlock) {
            HammerBaseBlock baseBlock = (HammerBaseBlock) world.getBlockState(basePos).getBlock();

            if (baseBlock.isFueled(world, basePos)) {
                return baseBlock.getHammerLevel(world, basePos);
            }
        }
        return super.getToolLevel(world, pos, blockState, toolType);
    }

    @Override
    public ItemStack onCraftConsumeCapability(World world, BlockPos pos, BlockState blockState, ItemStack targetStack, PlayerEntity player,
            ToolType requiredTool, int requiredLevel, boolean consumeResources) {
        BlockPos basePos = pos.up();
        if (consumeResources && world.getBlockState(basePos).getBlock() instanceof HammerBaseBlock) {
            HammerBaseBlock baseBlock = (HammerBaseBlock) world.getBlockState(basePos).getBlock();
            baseBlock.consumeFuel(world, basePos);

            baseBlock.applyEffects(world, basePos, targetStack, player);

            TileEntityOptional.from(world, pos, HammerHeadTile.class).ifPresent(HammerHeadTile::activate);
            world.playSound(player, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 3f, (float) (0.5 + Math.random() * 0.1));
        }
        return targetStack;
    }

    @Override
    public ItemStack onActionConsumeCapability(World world, BlockPos pos, BlockState blockState, ItemStack targetStack, PlayerEntity player,
            ToolType requiredTool, int requiredLevel, boolean consumeResources) {
        BlockPos basePos = pos.up();
        if (consumeResources && world.getBlockState(basePos).getBlock() instanceof HammerBaseBlock) {
            HammerBaseBlock baseBlock = (HammerBaseBlock) world.getBlockState(basePos).getBlock();
            baseBlock.consumeFuel(world, basePos);

            TileEntityOptional.from(world, pos, HammerHeadTile.class).ifPresent(HammerHeadTile::activate);
            world.playSound(player, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 3f, (float) (0.5 + Math.random() * 0.1));
        }
        return targetStack;
    }

    @Override
    public BlockState updatePostPlacement(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos, BlockPos facingPos) {
        if (Direction.UP.equals(facing) && !HammerBaseBlock.instance.equals(facingState.getBlock())) {
            return state.get(BlockStateProperties.WATERLOGGED) ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState();
        }

        return super.updatePostPlacement(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    public VoxelShape getShape(final BlockState blockState, final IBlockReader world, final BlockPos pos, final ISelectionContext context) {
        return shape;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world) {
        return new HammerHeadTile();
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }
}
