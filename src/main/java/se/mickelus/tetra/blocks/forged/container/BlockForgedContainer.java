package se.mickelus.tetra.blocks.forged.container;

import com.google.common.primitives.Booleans;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IBlockCapabilityInteractive;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.network.GuiHandlerRegistry;
import se.mickelus.tetra.network.PacketHandler;
import se.mickelus.tetra.network.TetraGuiHandler;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Predicates.equalTo;

public class BlockForgedContainer extends TetraBlock implements ITileEntityProvider, IBlockCapabilityInteractive {
    public static final PropertyDirection propFacing = BlockHorizontal.FACING;
    public static final PropertyBool propFlipped = PropertyBool.create("flipped");
    public static final PropertyBool propLocked1 = PropertyBool.create("locked1");
    public static final PropertyBool propLocked2 = PropertyBool.create("locked2");
    public static final PropertyBool propLockedAdjacent = PropertyBool.create("adjacent");
    public static final PropertyBool propOpen = PropertyBool.create("open");

    public static final BlockInteraction[] interactions = new BlockInteraction[]{
            new BlockInteraction(Capability.hammer, 3, EnumFacing.SOUTH, 5, 7, 2, 5,
                    new PropertyMatcher().where(propLocked1, equalTo(true)).where(propFlipped, equalTo(false)),
                    BlockForgedContainer::breakLock0),
            new BlockInteraction(Capability.hammer, 3, EnumFacing.SOUTH, 11, 13, 2, 5,
                    new PropertyMatcher().where(propLocked2, equalTo(true)).where(propFlipped, equalTo(false)),
                    BlockForgedContainer::breakLock1),
            new BlockInteraction(Capability.hammer, 3, EnumFacing.SOUTH, 17, 19, 2, 5,
                    new PropertyMatcher().where(propLocked1, equalTo(true)).where(propFlipped, equalTo(true)),
                    BlockForgedContainer::breakLock2),
            new BlockInteraction(Capability.hammer, 3, EnumFacing.SOUTH, 23, 25, 2, 5,
                    new PropertyMatcher().where(propLocked2, equalTo(true)).where(propFlipped, equalTo(true)),
                    BlockForgedContainer::breakLock3),
            new BlockInteraction(Capability.pry, 1, EnumFacing.SOUTH, 1, 16, 3, 4,
                    new PropertyMatcher()
                            .where(propLocked1, equalTo(false))
                            .where(propLocked2, equalTo(false))
                            .where(propLockedAdjacent, equalTo(false))
                            .where(propOpen, equalTo(false))
                            .where(propFlipped, equalTo(false)),
                    BlockForgedContainer::open),
            new BlockInteraction(Capability.pry, 1, EnumFacing.SOUTH, 0, 15, 3, 4,
                    new PropertyMatcher()
                            .where(propLocked1, equalTo(false))
                            .where(propLocked2, equalTo(false))
                            .where(propLockedAdjacent, equalTo(false))
                            .where(propOpen, equalTo(false))
                            .where(propFlipped, equalTo(true)),
                    BlockForgedContainer::open)
    };

    private static AxisAlignedBB aabbZ1 = new AxisAlignedBB(0.0625,  0.0, -0.9375, 0.9375, 0.75, 0.9375);
    private static AxisAlignedBB aabbZ2 = new AxisAlignedBB(0.0625,  0.0, 0.0625,  0.9375, 0.75, 1.9375);
    private static AxisAlignedBB aabbX1 = new AxisAlignedBB(-0.9375, 0.0, 0.0625,  0.9375, 0.75, 0.9375);
    private static AxisAlignedBB aabbX2 = new AxisAlignedBB(0.0625,  0.0, 0.0625,  1.9375, 0.75, 0.9375);

    public static final String unlocalizedName = "forged_container";

    @GameRegistry.ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockForgedContainer instance;

    public BlockForgedContainer() {
        super(Material.IRON);
        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        GameRegistry.registerTileEntity(TileEntityForgedContainer.class, new ResourceLocation(TetraMod.MOD_ID, unlocalizedName));
        setCreativeTab(TetraCreativeTabs.getInstance());

        setBlockUnbreakable();

        hasItem = true;

        setDefaultState(getBlockState().getBaseState()
                .withProperty(propFacing, EnumFacing.EAST)
                .withProperty(propFlipped, false));
    }

    @Override
    public void init(PacketHandler packetHandler) {
        GuiHandlerRegistry.instance.registerHandler(TetraGuiHandler.forgedContainerId, new GuiHandlerForgedContainer());
        packetHandler.registerPacket(ChangeCompartmentPacket.class, Side.SERVER);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        drops.clear();
    }

    /**
     * Special item registration to to check that multiblock is allowed to be placed
     * @param registry Item registry
     */
    @Override
    public void registerItem(IForgeRegistry<Item> registry) {
        Item item = new ItemBlockForgedContainer(this);
        item.setRegistryName(getRegistryName());
        registry.register(item);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(ChatFormatting.DARK_GRAY + I18n.format("forged_description"));
    }

    private static void breakLock(IBlockAccess world, BlockPos pos, EntityPlayer player, int index) {
        TileEntityForgedContainer te = (TileEntityForgedContainer) world.getTileEntity(pos);
        if (te != null) {
            te.getOrDelegate().breakLock(player, index);
        }
    }

    private static boolean breakLock0(IBlockAccess world, BlockPos pos, IBlockState blockState, EntityPlayer player,
            EnumHand hand, EnumFacing facing) {
        breakLock(world, pos, player, 0);
        return true;
    }

    private static boolean breakLock1(IBlockAccess world, BlockPos pos, IBlockState blockState, EntityPlayer player,
            EnumHand hand, EnumFacing facing) {
        breakLock(world, pos, player, 1);
        return true;
    }

    private static boolean breakLock2(IBlockAccess world, BlockPos pos, IBlockState blockState, EntityPlayer player,
            EnumHand hand, EnumFacing facing) {
        breakLock(world, pos, player, 2);
        return true;
    }

    private static boolean breakLock3(IBlockAccess world, BlockPos pos, IBlockState blockState, EntityPlayer player,
            EnumHand hand, EnumFacing facing) {
        breakLock(world, pos, player, 3);
        return true;
    }

    private static boolean open(IBlockAccess world, BlockPos pos, IBlockState blockState, EntityPlayer player,
            EnumHand hand, EnumFacing facing) {

        TileEntityForgedContainer te = (TileEntityForgedContainer) world.getTileEntity(pos);
        if (te != null) {
            te.getOrDelegate().open(player);
        }

        return true;
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(IBlockState state, EnumFacing face, Collection<Capability> capabilities) {
        return Arrays.stream(interactions)
                .filter(interaction -> interaction.isPotentialInteraction(state, state.getValue(propFacing), face, capabilities))
                .toArray(BlockInteraction[]::new);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ) {
        boolean didInteract = BlockInteraction.attemptInteraction(world, getActualState(world.getBlockState(pos), world, pos), pos, player, hand,
                facing, hitX, hitY, hitZ);

        if (!didInteract) {
            TileEntityForgedContainer te = (TileEntityForgedContainer) world.getTileEntity(pos);
            if (te != null) {
                if (te.getOrDelegate().isOpen()) {
                    player.openGui(TetraMod.instance, TetraGuiHandler.forgedContainerId, world, pos.getX(), pos.getY(), pos.getZ());
                }
            }
        } else {
            world.notifyBlockUpdate(pos, state, state, 3);
        }

        return true;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean causesSuffocation(IBlockState state) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        AxisAlignedBB aabb = null;

        state = getActualState(state, source, pos);
        EnumFacing facing = state.getValue(propFacing);
        boolean flipped = state.getValue(propFlipped);
        boolean open = state.getValue(propOpen);

        if (flipped) {
            switch (facing) {
                case NORTH:
                    aabb = aabbX1;
                    break;
                case EAST:
                    aabb = aabbZ1;
                    break;
                case SOUTH:
                    aabb = aabbX2;
                    break;
                case WEST:
                    aabb = aabbZ2;
                    break;
            }
        } else {
            switch (facing) {
                case NORTH:
                    aabb = aabbX2;
                    break;
                case EAST:
                    aabb = aabbZ2;
                    break;
                case SOUTH:
                    aabb = aabbX1;
                    break;
                case WEST:
                    aabb = aabbZ1;
                    break;
            }
        }

        if (open && aabb != null) {
            aabb = aabb.setMaxY(0.5625);
        }

        return aabb;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, propFacing, propFlipped, propLocked1, propLocked2, propLockedAdjacent, propOpen);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        IBlockState actualState = super.getExtendedState(state, world, pos);
        TileEntityForgedContainer te = (TileEntityForgedContainer) world.getTileEntity(pos);

        if (te != null) {
            te = te.getOrDelegate();

            boolean anyLocked = Arrays.stream(te.getOrDelegate().isLocked()).anyMatch(isLocked -> isLocked);

            if (state.getValue(propFlipped)) {
                actualState = actualState
                        .withProperty(propLocked1, te.isLocked(2))
                        .withProperty(propLocked2, te.isLocked(3));
            } else {
                actualState =  actualState
                        .withProperty(propLocked1, te.isLocked(0))
                        .withProperty(propLocked2, te.isLocked(1));
            }

            actualState =  actualState
                    .withProperty(propOpen, te.isOpen())
                    .withProperty(propLockedAdjacent, anyLocked);
        }

        return actualState;
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

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        IBlockState iblockstate = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer);

        return iblockstate.withProperty(propFacing, placer.getHorizontalFacing());
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        EnumFacing facing = state.getValue(propFacing);
        worldIn.setBlockState(pos.offset(facing.rotateY()), getDefaultState().withProperty(propFlipped, true).withProperty(propFacing, facing));
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        BlockPos relativePos;
        if (state.getValue(propFlipped)) {
            relativePos = pos.offset(state.getValue(propFacing).rotateYCCW());
        } else {
            relativePos = pos.offset(state.getValue(propFacing).rotateY());
        }

        if (!equals(world.getBlockState(relativePos).getBlock())) {
            world.setBlockToAir(pos);
        }
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        EnumFacing facing = state.getValue(propFacing);

        if (Rotation.CLOCKWISE_180.equals(rot)
                || Rotation.CLOCKWISE_90.equals(rot) && ( EnumFacing.NORTH.equals(facing) || EnumFacing.SOUTH.equals(facing))
                || Rotation.COUNTERCLOCKWISE_90.equals(rot) && ( EnumFacing.EAST.equals(facing) || EnumFacing.WEST.equals(facing))) {
            state = state.withProperty(propFlipped, state.getValue(propFlipped));
        }

        return state.withProperty(propFacing, rot.rotate(facing));
    }
}
