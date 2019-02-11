package se.mickelus.tetra.blocks.forged.container;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.network.TetraGuiHandler;

public class BlockForgedContainer extends TetraBlock implements ITileEntityProvider {
    public static final PropertyDirection propFacing = BlockHorizontal.FACING;
    public static final PropertyBool propFlipped = PropertyBool.create("flipped");

    protected static final AxisAlignedBB singleAABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.875D, 0.9375D);

    public static final String unlocalizedName = "forged_container";

    @GameRegistry.ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockForgedContainer instance;

    public BlockForgedContainer() {
        super(Material.IRON);

        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        GameRegistry.registerTileEntity(TileEntityForgedContainer.class, new ResourceLocation(TetraMod.MOD_ID, unlocalizedName));
        setCreativeTab(TetraCreativeTabs.getInstance());

        hasItem = true;

        setDefaultState(getBlockState().getBaseState()
                .withProperty(propFacing, EnumFacing.EAST)
                .withProperty(propFlipped, false));
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ) {
        player.openGui(TetraMod.instance, TetraGuiHandler.guiForgedContainerId, world, pos.getX(), pos.getY(), pos.getZ());

        return true;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, propFacing, propFlipped);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return super.getDefaultState()
                .withProperty(propFacing, EnumFacing.getHorizontal(meta & 0b11))
                .withProperty(propFlipped, ( meta >> 2 & 1 ) == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(propFacing).getHorizontalIndex()
                | (state.getValue(propFlipped) ? 1 << 2 : 0);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityForgedContainer();
    }
}
