package se.mickelus.tetra.blocks.forged.container;

import net.minecraft.block.*;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
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
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.blocks.TetraWaterloggedBlock;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IInteractiveBlock;
import se.mickelus.tetra.network.PacketHandler;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Predicates.equalTo;

public class ForgedContainerBlock extends TetraWaterloggedBlock implements IInteractiveBlock {
    public static final String unlocalizedName = "forged_container";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ForgedContainerBlock instance;

    public static final DirectionProperty facingProp = HorizontalBlock.FACING;
    public static final BooleanProperty flippedProp = BooleanProperty.create("flipped");
    public static final BooleanProperty locked1Prop = BooleanProperty.create("locked1");
    public static final BooleanProperty locked2Prop = BooleanProperty.create("locked2");
    public static final BooleanProperty anyLockedProp = BooleanProperty.create("locked_any");
    public static final BooleanProperty openProp = BooleanProperty.create("open");

    public static final BlockInteraction[] interactions = new BlockInteraction[]{
            new BlockInteraction(ToolTypes.hammer, 3, Direction.SOUTH, 5, 7, 2, 5,
                    new PropertyMatcher().where(locked1Prop, equalTo(true)).where(flippedProp, equalTo(false)),
                    (world, pos, blockState, player, hand, hitFace) -> breakLock(world, pos, player, 0, hand)),
            new BlockInteraction(ToolTypes.hammer, 3, Direction.SOUTH, 11, 13, 2, 5,
                    new PropertyMatcher().where(locked2Prop, equalTo(true)).where(flippedProp, equalTo(false)),
                    (world, pos, blockState, player, hand, hitFace) -> breakLock(world, pos, player, 1, hand)),
            new BlockInteraction(ToolTypes.hammer, 3, Direction.SOUTH, 17, 19, 2, 5,
                    new PropertyMatcher().where(locked1Prop, equalTo(true)).where(flippedProp, equalTo(true)),
                    (world, pos, blockState, player, hand, hitFace) -> breakLock(world, pos, player, 2, hand)),
            new BlockInteraction(ToolTypes.hammer, 3, Direction.SOUTH, 23, 25, 2, 5,
                    new PropertyMatcher().where(locked2Prop, equalTo(true)).where(flippedProp, equalTo(true)),
                    (world, pos, blockState, player, hand, hitFace) -> breakLock(world, pos, player, 3, hand)),
            new BlockInteraction(ToolTypes.pry, 1, Direction.SOUTH, 1, 15, 3, 4,
                    new PropertyMatcher()
                            .where(anyLockedProp, equalTo(false))
                            .where(openProp, equalTo(false))
                            .where(flippedProp, equalTo(false)),
                    ForgedContainerBlock::open),
            new BlockInteraction(ToolTypes.pry, 1, Direction.SOUTH, 15, 28, 3, 4,
                    new PropertyMatcher()
                            .where(anyLockedProp, equalTo(false))
                            .where(openProp, equalTo(false))
                            .where(flippedProp, equalTo(true)),
                    ForgedContainerBlock::open)
    };

    private static final VoxelShape shapeZ1 =     box(1,   0, -15, 15, 12, 15);
    private static final VoxelShape shapeZ2 =     box(1,   0, 1,   15, 12, 31);
    private static final VoxelShape shapeX1 =     box(-15, 0, 1,   15, 12, 15);
    private static final VoxelShape shapeX2 =     box(1,   0, 1,   31, 12, 15);
    private static final VoxelShape shapeZ1Open = box(1,   0, -15, 15,  9, 15);
    private static final VoxelShape shapeZ2Open = box(1,   0, 1,   15,  9, 31);
    private static final VoxelShape shapeX1Open = box(-15, 0, 1,   15,  9, 15);
    private static final VoxelShape shapeX2Open = box(1,   0, 1,   31,  9, 15);

    public ForgedContainerBlock() {
        super(ForgedBlockCommon.propertiesSolid);

        setRegistryName(unlocalizedName);

        hasItem = true;

        registerDefaultState(defaultBlockState()
                .setValue(facingProp, Direction.EAST)
                .setValue(flippedProp, false)
                .setValue(openProp, true)
                .setValue(locked1Prop, false)
                .setValue(locked2Prop, false)
                .setValue(anyLockedProp, false));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void clientInit() {
        ClientRegistry.bindTileEntityRenderer(ForgedContainerTile.type, ForgedContainerRenderer::new);
        ScreenManager.register(ForgedContainerContainer.type, ForgedContainerScreen::new);
    }

    @Override
    public void init(PacketHandler packetHandler) {
        packetHandler.registerPacket(ChangeCompartmentPacket.class, ChangeCompartmentPacket::new);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(ForgedBlockCommon.locationTooltip);
    }

    private static boolean breakLock(World world, BlockPos pos, @Nullable PlayerEntity player, int index, @Nullable Hand hand) {
        ForgedContainerTile te = (ForgedContainerTile) world.getBlockEntity(pos);
        if (te != null) {
            te.getOrDelegate().breakLock(player, index, hand);
        }

        return true;
    }

    private static boolean open(World world, BlockPos pos, BlockState blockState, PlayerEntity player, Hand hand, Direction facing) {
        ForgedContainerTile te = (ForgedContainerTile) world.getBlockEntity(pos);
        if (te != null) {
            te.getOrDelegate().open(player);
        }

        return true;
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(World world, BlockPos pos, BlockState state, Direction face, Collection<ToolType> tools) {
        return Arrays.stream(interactions)
                .filter(interaction -> interaction.isPotentialInteraction(world, pos, state, state.getValue(facingProp), face, tools))
                .toArray(BlockInteraction[]::new);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        ActionResultType didInteract = BlockInteraction.attemptInteraction(world, state, pos, player, hand, hit);

        if (didInteract != ActionResultType.SUCCESS) {
            if (!world.isClientSide) {
                TileEntityOptional.from(world, pos, ForgedContainerTile.class)
                        .ifPresent(te -> {
                            ForgedContainerTile delegate = te.getOrDelegate();
                            if (delegate.isOpen()) {
                                NetworkHooks.openGui((ServerPlayerEntity) player, delegate, delegate.getBlockPos());
                            }
                        });
            }
        } else {
            world.sendBlockUpdated(pos, state, state, 3);
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!equals(newState.getBlock())) {
            // only drop loot from open, primary/unflipped chests
            if (state.getValue(openProp) && !state.getValue(flippedProp)) {
                dropBlockInventory(this, world, pos, newState);
            } else {
                TileEntityOptional.from(world, pos, ForgedContainerTile.class).ifPresent(TileEntity::setRemoved);
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        Direction facing = state.getValue(facingProp);
        boolean flipped = state.getValue(flippedProp);

        if (state.getValue(openProp)) {
            if (flipped) {
                switch (facing) {
                    case NORTH:
                        return shapeX1Open;
                    case EAST:
                        return shapeZ1Open;
                    case SOUTH:
                        return shapeX2Open;
                    case WEST:
                        return shapeZ2Open;
                }
            } else {
                switch (facing) {
                    case NORTH:
                        return shapeX2Open;
                    case EAST:
                        return shapeZ2Open;
                    case SOUTH:
                        return shapeX1Open;
                    case WEST:
                        return shapeZ1Open;
                }
            }
        } else {
            if (flipped) {
                switch (facing) {
                    case NORTH:
                        return shapeX1;
                    case EAST:
                        return shapeZ1;
                    case SOUTH:
                        return shapeX2;
                    case WEST:
                        return shapeZ2;
                }
            } else {
                switch (facing) {
                    case NORTH:
                        return shapeX2;
                    case EAST:
                        return shapeZ2;
                    case SOUTH:
                        return shapeX1;
                    case WEST:
                        return shapeZ1;
                }
            }
        }

        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(facingProp, flippedProp, locked1Prop, locked2Prop, anyLockedProp, openProp);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ForgedContainerTile();
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context) {
        if (context.getLevel().getBlockState(context.getClickedPos().relative(context.getHorizontalDirection().getClockWise())).canBeReplaced(context)) {
            return super.getStateForPlacement(context).setValue(facingProp, context.getHorizontalDirection());
        }

        // returning null here stops the block from being placed
        return null;
    }

    // based on same method implementation in BedBlock
    public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        Direction facing = state.getValue(facingProp);
        world.setBlock(pos.relative(facing.getClockWise()), defaultBlockState().setValue(flippedProp, true).setValue(facingProp, facing), 3);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos,
            BlockPos facingPos) {
        Direction pairedFacing = state.getValue(facingProp);
        if (state.getValue(flippedProp)) {
            pairedFacing = pairedFacing.getCounterClockWise();
        } else {
            pairedFacing = pairedFacing.getClockWise();
        }

        if (pairedFacing == facing && !equals(facingState.getBlock())) {
            return state.getValue(BlockStateProperties.WATERLOGGED) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        Direction facing = state.getValue(facingProp);

        if (Rotation.CLOCKWISE_180.equals(rot)
                || Rotation.CLOCKWISE_90.equals(rot) && ( Direction.NORTH.equals(facing) || Direction.SOUTH.equals(facing))
                || Rotation.COUNTERCLOCKWISE_90.equals(rot) && ( Direction.EAST.equals(facing) || Direction.WEST.equals(facing))) {
            state = state.setValue(flippedProp, state.getValue(flippedProp));
        }

        return state.setValue(facingProp, rot.rotate(facing));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(facingProp)));
    }
}
