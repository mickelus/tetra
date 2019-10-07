package se.mickelus.tetra.blocks.hammer;

import net.minecraft.util.text.TextFormatting;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.state.Property;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.TetraItemGroup;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BlockHammerHead extends TetraBlock implements ITileEntityProvider {

    static final String unlocalizedName = "hammer_head";
    public static final AxisAlignedBB boundingBox = new AxisAlignedBB(0.125, 0.8125, 0.125, 0.875, 1, 0.875);

    public static BlockHammerHead instance;

    public BlockHammerHead() {
        super(Material.IRON);

        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        GameRegistry.registerTileEntity(TileEntityHammerHead.class, TetraMod.MOD_ID + ":" + "tile_" +unlocalizedName);
        setCreativeTab(TetraItemGroup.getInstance());
        setBlockUnbreakable();

        hasItem = true;

        instance = this;

        this.setDefaultState(this.blockState.getBaseState());
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, BlockState state, int fortune) {
        drops.clear();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(TextFormatting.DARK_GRAY + I18n.format("forged_description"));
    }

    @Override
    public Collection<Capability> getCapabilities(World world, BlockPos pos, BlockState blockState) {
        BlockPos basePos = pos.offset(Direction.UP);
        if (world.getBlockState(basePos).getBlock() instanceof BlockHammerBase) {
            BlockHammerBase baseBlock = (BlockHammerBase) world.getBlockState(basePos).getBlock();

            if (baseBlock.isFueled(world, basePos)) {
                return Collections.singletonList(Capability.hammer);
            }
        }
        return super.getCapabilities(world, pos, blockState);
    }

    @Override
    public int getCapabilityLevel(World world, BlockPos pos, BlockState blockState, Capability capability) {
        BlockPos basePos = pos.offset(Direction.UP);
        if (Capability.hammer.equals(capability) && world.getBlockState(basePos).getBlock() instanceof BlockHammerBase) {
            BlockHammerBase baseBlock = (BlockHammerBase) world.getBlockState(basePos).getBlock();

            if (baseBlock.isFueled(world, basePos)) {
                return baseBlock.getHammerLevel(world, basePos);
            }
        }
        return super.getCapabilityLevel(world, pos, blockState, capability);
    }

    @Override
    public ItemStack onCraftConsumeCapability(World world, BlockPos pos, BlockState blockState, ItemStack targetStack, PlayerEntity player, boolean consumeResources) {
        BlockPos basePos = pos.offset(Direction.UP);
        if (consumeResources && world.getBlockState(basePos).getBlock() instanceof BlockHammerBase) {
            BlockHammerBase baseBlock = (BlockHammerBase) world.getBlockState(basePos).getBlock();
            baseBlock.consumeFuel(world, basePos);

            baseBlock.applyEffects(world, basePos, targetStack, player);

            ((TileEntityHammerHead) world.getTileEntity(pos)).activate();
            world.playSound(player, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 3f, (float) (0.5 + Math.random() * 0.1));
        }
        return targetStack;
    }

    @Override
    public ItemStack onActionConsumeCapability(World world, BlockPos pos, BlockState blockState, ItemStack targetStack, PlayerEntity player, boolean consumeResources) {
    BlockPos basePos = pos.offset(Direction.UP);
        if (consumeResources && world.getBlockState(basePos).getBlock() instanceof BlockHammerBase) {
            BlockHammerBase baseBlock = (BlockHammerBase) world.getBlockState(basePos).getBlock();
            baseBlock.consumeFuel(world, basePos);

            ((TileEntityHammerHead) world.getTileEntity(pos)).activate();
            world.playSound(player, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 3f, (float) (0.5 + Math.random() * 0.1));
        }
        return targetStack;
    }

    @Override
    public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos) {
        return boundingBox;
    }

    @Override
    public ExtendedBlockState createBlockState() {
        return new ExtendedBlockState(this, new Property[]{ Properties.StaticProperty }, new IUnlistedProperty[]{ Properties.AnimationProperty });
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityHammerHead();
    }

    @Override
    public BlockState getActualState(BlockState state, IBlockAccess world, BlockPos pos) {
        return state.withProperty(Properties.StaticProperty, false);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return 0;
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return getDefaultState();
    }

    @Override
    public EnumBlockRenderType getRenderType(BlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) { return false; }

    @Override
    public boolean isFullCube(BlockState state) { return false; }
}
