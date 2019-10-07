package se.mickelus.tetra.blocks.forged.container;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IBlockCapabilityInteractive;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.network.GuiHandlerRegistry;
import se.mickelus.tetra.network.PacketHandler;
import se.mickelus.tetra.network.TetraGuiHandler;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Predicates.equalTo;

public class BlockForgedContainer extends TetraBlock implements IBlockCapabilityInteractive {
    public static final DirectionProperty propFacing = HorizontalBlock.HORIZONTAL_FACING;
    public static final BooleanProperty propFlipped = BooleanProperty.create("flipped");
    public static final BooleanProperty propLocked1 = BooleanProperty.create("locked1");
    public static final BooleanProperty propLocked2 = BooleanProperty.create("locked2");
    public static final BooleanProperty propLockedAdjacent = BooleanProperty.create("adjacent");
    public static final BooleanProperty propOpen = BooleanProperty.create("open");

    public static final BlockInteraction[] interactions = new BlockInteraction[]{
            new BlockInteraction(Capability.hammer, 3, Direction.SOUTH, 5, 7, 2, 5,
                    new PropertyMatcher().where(propLocked1, equalTo(true)).where(propFlipped, equalTo(false)),
                    (world, pos, blockState, player, hand, facing) -> breakLock0(world, pos, blockState, player, hand, facing)),
            new BlockInteraction(Capability.hammer, 3, Direction.SOUTH, 11, 13, 2, 5,
                    new PropertyMatcher().where(propLocked2, equalTo(true)).where(propFlipped, equalTo(false)),
                    (world, pos, blockState, player, hand, facing) -> breakLock1(world, pos, blockState, player, hand, facing)),
            new BlockInteraction(Capability.hammer, 3, Direction.SOUTH, 17, 19, 2, 5,
                    new PropertyMatcher().where(propLocked1, equalTo(true)).where(propFlipped, equalTo(true)),
                    (world, pos, blockState, player, hand, facing) -> breakLock2(world, pos, blockState, player, hand, facing)),
            new BlockInteraction(Capability.hammer, 3, Direction.SOUTH, 23, 25, 2, 5,
                    new PropertyMatcher().where(propLocked2, equalTo(true)).where(propFlipped, equalTo(true)),
                    (world, pos, blockState, player, hand, facing) -> breakLock3(world, pos, blockState, player, hand, facing)),
            new BlockInteraction(Capability.pry, 1, Direction.SOUTH, 1, 15, 3, 4,
                    new PropertyMatcher()
                            .where(propLocked1, equalTo(false))
                            .where(propLocked2, equalTo(false))
                            .where(propLockedAdjacent, equalTo(false))
                            .where(propOpen, equalTo(false))
                            .where(propFlipped, equalTo(false)),
                    (world, pos, blockState, player, hand, facing) -> open(world, pos, blockState, player, hand, facing)),
            new BlockInteraction(Capability.pry, 1, Direction.SOUTH, 15, 28, 3, 4,
                    new PropertyMatcher()
                            .where(propLocked1, equalTo(false))
                            .where(propLocked2, equalTo(false))
                            .where(propLockedAdjacent, equalTo(false))
                            .where(propOpen, equalTo(false))
                            .where(propFlipped, equalTo(true)),
                    (world, pos, blockState, player, hand, facing) -> open(world, pos, blockState, player, hand, facing))
    };

    private static AxisAlignedBB aabbZ1 = new AxisAlignedBB(0.0625,  0.0, -0.9375, 0.9375, 0.75, 0.9375);
    private static AxisAlignedBB aabbZ2 = new AxisAlignedBB(0.0625,  0.0, 0.0625,  0.9375, 0.75, 1.9375);
    private static AxisAlignedBB aabbX1 = new AxisAlignedBB(-0.9375, 0.0, 0.0625,  0.9375, 0.75, 0.9375);
    private static AxisAlignedBB aabbX2 = new AxisAlignedBB(0.0625,  0.0, 0.0625,  1.9375, 0.75, 0.9375);

    public static final String unlocalizedName = "forged_container";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockForgedContainer instance;

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ContainerType<ForgedContainerContainer> containerType;

    public BlockForgedContainer() {
        super(Material.IRON);
        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        GameRegistry.registerTileEntity(TileEntityForgedContainer.class, new ResourceLocation(TetraMod.MOD_ID, unlocalizedName));
        setCreativeTab(TetraItemGroup.getInstance());

        setBlockUnbreakable();

        hasItem = true;

        setDefaultState(getBlockState().getBaseState()
                .withProperty(propFacing, Direction.EAST)
                .withProperty(propFlipped, false));
    }

    @Override
    public void init(PacketHandler packetHandler) {
        ScreenManager.registerFactory();
        GuiHandlerRegistry.instance.registerHandler(TetraGuiHandler.forgedContainerId, new GuiHandlerForgedContainer());
        packetHandler.registerPacket(ChangeCompartmentPacket.class, Side.SERVER);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, BlockState state, int fortune) {
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

        if (FMLEnvironment.dist.isClient()) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader player, List<ITextComponent> tooltip, ITooltipFlag advanced) {
        tooltip.add(new TranslationTextComponent("forged_description").setStyle(new Style().setColor(TextFormatting.DARK_GRAY)));
    }

    private static void breakLock(World world, BlockPos pos, PlayerEntity player, int index) {
        TileEntityForgedContainer te = (TileEntityForgedContainer) world.getTileEntity(pos);
        if (te != null) {
            te.getOrDelegate().breakLock(player, index);
        }
    }

    private static boolean breakLock0(World world, BlockPos pos, BlockState blockState, PlayerEntity player,
            Hand hand, Direction facing) {
        breakLock(world, pos, player, 0);
        return true;
    }

    private static boolean breakLock1(World world, BlockPos pos, BlockState blockState, PlayerEntity player,
            Hand hand, Direction facing) {
        breakLock(world, pos, player, 1);
        return true;
    }

    private static boolean breakLock2(World world, BlockPos pos, BlockState blockState, PlayerEntity player,
            Hand hand, Direction facing) {
        breakLock(world, pos, player, 2);
        return true;
    }

    private static boolean breakLock3(World world, BlockPos pos, BlockState blockState, PlayerEntity player,
            Hand hand, Direction facing) {
        breakLock(world, pos, player, 3);
        return true;
    }

    private static boolean open(World world, BlockPos pos, BlockState blockState, PlayerEntity player,
            Hand hand, Direction facing) {

        TileEntityForgedContainer te = (TileEntityForgedContainer) world.getTileEntity(pos);
        if (te != null) {
            te.getOrDelegate().open(player);
        }

        return true;
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(BlockState state, Direction face, Collection<Capability> capabilities) {
        return Arrays.stream(interactions)
                .filter(interaction -> interaction.isPotentialInteraction(state, state.getValue(propFacing), face, capabilities))
                .toArray(BlockInteraction[]::new);
    }

    @Override
    public boolean onBlockActivated(BlockState blockState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        Vec3d hit = rayTraceResult.getHitVec();
        boolean didInteract = BlockInteraction.attemptInteraction(world, getExtendedState(blockState, world, pos), pos, player, hand,
                rayTraceResult.getFace(), hit.x, hit.y, hit.z);

        if (!didInteract) {
            if (!world.isRemote) {
                TileEntityForgedContainer te = (TileEntityForgedContainer) world.getTileEntity(pos);
                if (te != null && te.getOrDelegate().isOpen()) {
                    NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) te, pos);
                }

            }
        } else {
            world.notifyBlockUpdate(pos, blockState, blockState, 3);
        }

        return true;
    }

    @Override
    public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos) {
        AxisAlignedBB aabb = null;

        state = getActualState(state, source, pos);
        Direction facing = state.getValue(propFacing);
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
    public BlockState getExtendedState(BlockState state, IBlockReader world, BlockPos pos) {
        BlockState actualState = super.getExtendedState(state, world, pos);
        TileEntityForgedContainer te = (TileEntityForgedContainer) world.getTileEntity(pos);

        if (te != null) {
            te = te.getOrDelegate();

            boolean anyLocked = Arrays.stream(te.getOrDelegate().isLocked()).anyMatch(isLocked -> isLocked);

            if (state.get(propFlipped)) {
                actualState = actualState
                        .with(propLocked1, te.isLocked(2))
                        .with(propLocked2, te.isLocked(3));
            } else {
                actualState =  actualState
                        .with(propLocked1, te.isLocked(0))
                        .with(propLocked2, te.isLocked(1));
            }

            actualState =  actualState
                    .with(propOpen, te.isOpen())
                    .with(propLockedAdjacent, anyLocked);
        }

        return actualState;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileEntityForgedContainer();
    }

    @Override
    public boolean hasTileEntity() {
        return true;
    }

    @Override
    public BlockState getStateForPlacement(World world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer, Hand hand) {
        BlockState BlockState = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer);

        return BlockState.withProperty(propFacing, placer.getHorizontalFacing());
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        Direction facing = state.getValue(propFacing);
        worldIn.setBlockState(pos.offset(facing.rotateY()), getDefaultState().withProperty(propFlipped, true).withProperty(propFacing, facing));
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
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
    public EnumBlockRenderType getRenderType(BlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockState withRotation(BlockState state, Rotation rot) {
        Direction facing = state.getValue(propFacing);

        if (Rotation.CLOCKWISE_180.equals(rot)
                || Rotation.CLOCKWISE_90.equals(rot) && ( Direction.NORTH.equals(facing) || Direction.SOUTH.equals(facing))
                || Rotation.COUNTERCLOCKWISE_90.equals(rot) && ( Direction.EAST.equals(facing) || Direction.WEST.equals(facing))) {
            state = state.withProperty(propFlipped, state.getValue(propFlipped));
        }

        return state.withProperty(propFacing, rot.rotate(facing));
    }
}
