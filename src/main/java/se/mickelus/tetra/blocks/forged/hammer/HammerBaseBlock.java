package se.mickelus.tetra.blocks.forged.hammer;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.advancements.BlockUseCriterion;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IBlockCapabilityInteractive;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;
import se.mickelus.tetra.items.forged.ItemVentPlate;
import se.mickelus.tetra.util.TileEntityOptional;

import static se.mickelus.tetra.blocks.forged.ForgedBlockCommon.hintTooltip;

public class HammerBaseBlock extends TetraBlock implements IBlockCapabilityInteractive {
    public static final String unlocalizedName = "hammer_base";

    public static final DirectionProperty propFacing = HorizontalBlock.HORIZONTAL_FACING;
    public static final BooleanProperty propCell1 = BooleanProperty.create("cell1");
    public static final BooleanProperty propCell1Charged = BooleanProperty.create("cell1charged");
    public static final BooleanProperty propCell2 = BooleanProperty.create("cell2");
    public static final BooleanProperty propCell2Charged = BooleanProperty.create("cell2charged");

    private static final ResourceLocation plateLootTable = new ResourceLocation(TetraMod.MOD_ID, "forged/plate_break");

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static HammerBaseBlock instance;

    public static final BlockInteraction[] interactions = new BlockInteraction[] {
            new BlockInteraction(Capability.pry, 1, EnumHammerPlate.EAST.face, 5, 11, 9, 11,
                    EnumHammerPlate.EAST.prop, true, (world, pos, blockState, player, hand, hitFace) ->
                    removePlate(world, pos, blockState, player, hand, EnumHammerPlate.EAST)),
            new BlockInteraction(Capability.pry, 1, EnumHammerPlate.WEST.face, 5, 11, 9, 11,
                    EnumHammerPlate.WEST.prop, true, (world, pos, blockState, player, hand, hitFace) ->
                    removePlate(world, pos, blockState, player, hand, EnumHammerPlate.WEST)),

            new BlockInteraction(Capability.hammer, 1, Direction.EAST, 6, 10, 2, 9,
                    EnumHammerPlate.EAST.prop, false, (world, pos, blockState, player, hand, hitFace) ->
                    reconfigure(world, pos, blockState, player, Direction.EAST)),
            new BlockInteraction(Capability.hammer, 1, Direction.WEST, 6, 10, 2, 9,
                    EnumHammerPlate.WEST.prop, false, (world, pos, blockState, player, hand, hitFace) ->
                    reconfigure(world, pos, blockState, player, Direction.WEST))
    };

    public HammerBaseBlock() {
        super(ForgedBlockCommon.properties);

        setRegistryName(unlocalizedName);

        hasItem = true;
    }

    @Override
    public void addInformation(final ItemStack stack, @Nullable final IBlockReader world, final List<ITextComponent> tooltip, final ITooltipFlag advanced) {
        tooltip.add(hintTooltip);
    }

    @Override
    public BlockState getExtendedState(final BlockState state, final IBlockReader world, final BlockPos pos) {
        return TileEntityOptional.from(world, pos, HammerBaseTile.class)
                .map(te -> state
                        .with(propCell1, te.hasCellInSlot(0))
                        .with(propCell1Charged, te.getCellFuel(0) > 0)
                        .with(propCell2, te.hasCellInSlot(1))
                        .with(propCell2Charged, te.getCellFuel(1) > 0)
                        .with(EnumHammerPlate.EAST.prop, te.hasPlate(EnumHammerPlate.EAST))
                        .with(EnumHammerPlate.WEST.prop, te.hasPlate(EnumHammerPlate.WEST))
                        .with(EnumHammerConfig.propE, te.getConfiguration(Direction.EAST))
                        .with(EnumHammerConfig.propW, te.getConfiguration(Direction.WEST)))
                .orElse(state);
    }

    public boolean isFueled(World world, BlockPos pos) {
        return TileEntityOptional.from(world, pos, HammerBaseTile.class)
                .map(HammerBaseTile::isFueled)
                .orElse(false);
    }

    public void consumeFuel(World world, BlockPos pos) {
        TileEntityOptional.from(world, pos, HammerBaseTile.class)
                .ifPresent(te -> {
                    BlockState blockState = world.getBlockState(pos);
                    te.consumeFuel();

                    world.notifyBlockUpdate(pos, blockState, blockState, 3);
                });
    }

    public void applyEffects(World world, BlockPos pos, ItemStack itemStack, PlayerEntity player) {
        TileEntityOptional.from(world, pos, HammerBaseTile.class)
                .ifPresent(te -> {
                    if (te.hasEffect(EnumHammerEffect.DAMAGING) && itemStack.getItem() instanceof ItemModular) {
                        ItemModular item = (ItemModular) itemStack.getItem();
                        int damage = (int) (itemStack.getMaxDamage() * 0.1);
                        item.applyDamage(damage, itemStack, player);
                    }
                });
    }

    public int getHammerLevel(World world, BlockPos pos) {
        return TileEntityOptional.from(world, pos, HammerBaseTile.class)
                .map(HammerBaseTile::getHammerLevel)
                .orElse(0);
    }

    public static boolean removePlate(World world, BlockPos pos, BlockState blockState, PlayerEntity player, Hand hand, EnumHammerPlate plate) {
        TileEntityOptional.from(world, pos, HammerBaseTile.class)
                .ifPresent(te -> {
                    te.removePlate(plate);

                    if (!world.isRemote) {
                        BlockInteraction.dropLoot(plateLootTable, player, hand, (ServerWorld) world, blockState);
                    }

                    world.playSound(player, pos, SoundEvents.ITEM_SHIELD_BREAK, SoundCategory.PLAYERS, 1, 0.5f);
                    world.notifyBlockUpdate(pos, blockState, blockState, 3);
        });

        return true;
    }

    public static boolean reconfigure(World world, BlockPos pos, BlockState blockState, PlayerEntity player, Direction adjustedFace) {
        TileEntityOptional.from(world, pos, HammerBaseTile.class)
                .ifPresent(te -> {
                    te.reconfigure(adjustedFace);
                    world.playSound(player, pos, SoundEvents.BLOCK_ANVIL_HIT, SoundCategory.PLAYERS, 1, 1);
                    world.notifyBlockUpdate(pos, blockState, blockState, 3);
        });

        return true;
    }

    @Override
    public boolean onBlockActivated(final BlockState blockState, final World world, final BlockPos pos, final PlayerEntity player, final Hand hand,
            final BlockRayTraceResult rayTraceResult) {
        Direction blockFacing = blockState.get(propFacing);
        HammerBaseTile te = TileEntityOptional.from(world, pos, HammerBaseTile.class).orElse(null);
        ItemStack heldStack = player.getHeldItem(hand);
        Direction facing = rayTraceResult.getFace();

        if (te == null) {
            return false;
        }

        if (blockFacing.getAxis().equals(facing.getAxis())) {
            int slotIndex = blockFacing.equals(facing)? 0 : 1;
            if (te.hasCellInSlot(slotIndex)) {
                ItemStack cell = te.removeCellFromSlot(slotIndex);
                if (player.inventory.addItemStackToInventory(cell)) {
                    player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1, 1);
                } else {
                    spawnAsEntity(world, pos.up(), cell);
                }

                world.playSound(player, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.PLAYERS, 0.5f, 0.6f);
                world.notifyBlockUpdate(pos, blockState, blockState, 3);

                if (!player.world.isRemote) {
                    BlockUseCriterion.trigger((ServerPlayerEntity) player, getExtendedState(blockState, world, pos), ItemStack.EMPTY);
                }

                return true;
            } else if (heldStack.getItem() instanceof ItemCellMagmatic) {
                te.putCellInSlot(heldStack, slotIndex);
                player.setHeldItem(hand, ItemStack.EMPTY);
                world.playSound(player, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.PLAYERS, 0.5f, 0.5f);
                world.notifyBlockUpdate(pos, blockState, blockState, 3);

                if (!player.world.isRemote) {
                    BlockUseCriterion.trigger((ServerPlayerEntity) player, getExtendedState(blockState, world, pos), heldStack);
                }

                return true;
            }
        } else if (heldStack.getItem() instanceof ItemVentPlate) {
            if (Rotation.CLOCKWISE_90.rotate(blockFacing).equals(facing) && !te.hasPlate(EnumHammerPlate.EAST)) {
                te.attachPlate(EnumHammerPlate.EAST);
                world.notifyBlockUpdate(pos, blockState, blockState, 3);

                if (!player.world.isRemote) {
                    BlockUseCriterion.trigger((ServerPlayerEntity) player, getExtendedState(blockState, world, pos), heldStack);
                }

                heldStack.shrink(1);

                return true;
            } else if (Rotation.COUNTERCLOCKWISE_90.rotate(blockFacing).equals(facing) && !te.hasPlate(EnumHammerPlate.WEST)) {
                te.attachPlate(EnumHammerPlate.WEST);
                world.notifyBlockUpdate(pos, blockState, blockState, 3);

                if (!player.world.isRemote) {
                    BlockUseCriterion.trigger((ServerPlayerEntity) player, getExtendedState(blockState, world, pos), heldStack);
                }

                heldStack.shrink(1);

                return true;
            }
        }

        return BlockInteraction.attemptInteraction(world, getExtendedState(blockState, world, pos), pos, player, hand, rayTraceResult);
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(final BlockState state, final Direction face, final Collection<Capability> capabilities) {
        return Arrays.stream(interactions)
                .filter(interaction -> interaction.isPotentialInteraction(state, state.get(propFacing), face, capabilities))
                .toArray(BlockInteraction[]::new);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean hasTileEntity(final BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world) {
        return new HammerBaseTile();
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder) {
        builder.add(propFacing, propCell1, propCell1Charged, propCell2 , propCell2Charged,
                EnumHammerPlate.EAST.prop, EnumHammerPlate.WEST.prop, EnumHammerConfig.propE, EnumHammerConfig.propW);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context) {
        return this.getDefaultState().with(propFacing, context.getPlacementHorizontalFacing().getOpposite());
    }

    @Override
    public BlockState rotate(final BlockState state, final Rotation rotation) {
        return state.with(propFacing, rotation.rotate(state.get(propFacing)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.toRotation(state.get(propFacing)));
    }
}
