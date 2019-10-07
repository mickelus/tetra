package se.mickelus.tetra.blocks.forged;

import net.minecraft.util.text.TextFormatting;
import net.minecraft.block.BlockSlab;
import net.minecraft.state.EnumProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.Materials;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.items.TetraItemGroup;

import javax.annotation.Nullable;
import java.util.List;


public class BlockForgedPlatformSlab extends TetraBlock {
    public static final EnumProperty<BlockSlab.EnumBlockHalf> halfProp = EnumProperty.<BlockSlab.EnumBlockHalf>create("half", BlockSlab.EnumBlockHalf.class);
    protected static final AxisAlignedBB AABB_BOTTOM_HALF = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
    protected static final AxisAlignedBB AABB_TOP_HALF = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 1.0D, 1.0D, 1.0D);

    static final String unlocalizedName = "forged_platform_slab";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockForgedPlatformSlab instance;

    public BlockForgedPlatformSlab() {
        super(Materials.forged);

        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        setCreativeTab(TetraItemGroup.getInstance());
        setBlockUnbreakable();

        hasItem = true;

        fullBlock = false;

        this.setDefaultState(this.blockState.getBaseState().withProperty(halfProp, BlockSlab.EnumBlockHalf.BOTTOM));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(TextFormatting.DARK_GRAY + I18n.format("forged_description"));
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, BlockState state, int fortune) {
        drops.clear();
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        if (explosion.getPosition().y < pos.getY()) {
            return 4;
        }

        return 6;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, halfProp);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(halfProp, meta == 1 ? BlockSlab.EnumBlockHalf.TOP : BlockSlab.EnumBlockHalf.BOTTOM);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        if (state.getValue(halfProp) == BlockSlab.EnumBlockHalf.TOP) {
            return 1;
        }

        return 0;
    }

    @Override
    public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos) {
        return state.getValue(halfProp) == BlockSlab.EnumBlockHalf.TOP ? AABB_TOP_HALF : AABB_BOTTOM_HALF;
    }

    @Override
    public boolean isTopSolid(BlockState state) {
        return state.getValue(halfProp) == BlockSlab.EnumBlockHalf.TOP;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, BlockState state, BlockPos pos, Direction face) {
        if (face == Direction.UP && state.getValue(halfProp) == BlockSlab.EnumBlockHalf.TOP) {
            return BlockFaceShape.SOLID;
        } else if (face == Direction.DOWN && state.getValue(halfProp) == BlockSlab.EnumBlockHalf.BOTTOM) {
            return BlockFaceShape.SOLID;
        }
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public boolean doesSideBlockRendering(BlockState state, IBlockAccess world, BlockPos pos, Direction face) {
        if (net.minecraftforge.common.ForgeModContainer.disableStairSlabCulling) {
            return false;
        }

        BlockSlab.EnumBlockHalf side = state.getValue(halfProp);
        return (side == BlockSlab.EnumBlockHalf.TOP && face == Direction.UP) || (side == BlockSlab.EnumBlockHalf.BOTTOM && face == Direction.DOWN);
    }

    /**
     * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
     * BlockState
     */
    @Override
    public BlockState getStateForPlacement(World worldIn, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
        BlockState BlockState = super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(halfProp, BlockSlab.EnumBlockHalf.BOTTOM);

        return facing != Direction.DOWN && (facing == Direction.UP || (double)hitY <= 0.5D) ? BlockState : BlockState.withProperty(halfProp, BlockSlab.EnumBlockHalf.TOP);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldSideBeRendered(BlockState blockState, IBlockAccess blockAccess, BlockPos pos, Direction side) {
        if (super.shouldSideBeRendered(blockState, blockAccess, pos, side)) {
            return true;
        }
        return !(side != Direction.UP && side != Direction.DOWN);
    }
}