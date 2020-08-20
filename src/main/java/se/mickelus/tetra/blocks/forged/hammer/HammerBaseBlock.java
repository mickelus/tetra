package se.mickelus.tetra.blocks.forged.hammer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.advancements.BlockUseCriterion;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IInteractiveBlock;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;
import se.mickelus.tetra.items.forged.ItemVentPlate;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static net.minecraft.fluid.Fluids.WATER;
import static net.minecraft.state.properties.BlockStateProperties.WATERLOGGED;
import static se.mickelus.tetra.blocks.forged.ForgedBlockCommon.locationTooltip;

public class HammerBaseBlock extends TetraBlock implements IInteractiveBlock {
    public static final DirectionProperty propFacing = HorizontalBlock.HORIZONTAL_FACING;
    public static final BooleanProperty propCell1 = BooleanProperty.create("cell1");
    public static final BooleanProperty propCell1Charged = BooleanProperty.create("cell1charged");
    public static final BooleanProperty propCell2 = BooleanProperty.create("cell2");
    public static final BooleanProperty propCell2Charged = BooleanProperty.create("cell2charged");

    private static final ResourceLocation plateLootTable = new ResourceLocation(TetraMod.MOD_ID, "forged/plate_break");

    public static final String unlocalizedName = "hammer_base";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static HammerBaseBlock instance;

    public static final BlockInteraction[] interactions = new BlockInteraction[] {
            new BlockInteraction(ToolTypes.pry, 1, EnumHammerPlate.east.face, 5, 11, 9, 11,
                    EnumHammerPlate.east.prop, true, (world, pos, blockState, player, hand, hitFace) ->
                    removePlate(world, pos, blockState, player, hand, EnumHammerPlate.east)),
            new BlockInteraction(ToolTypes.pry, 1, EnumHammerPlate.west.face, 5, 11, 9, 11,
                    EnumHammerPlate.west.prop, true, (world, pos, blockState, player, hand, hitFace) ->
                    removePlate(world, pos, blockState, player, hand, EnumHammerPlate.west)),

            new BlockInteraction(ToolTypes.hammer, 1, Direction.EAST, 6, 10, 2, 9,
                    EnumHammerPlate.east.prop, false, (world, pos, blockState, player, hand, hitFace) ->
                    reconfigure(world, pos, blockState, player, Direction.EAST)),
            new BlockInteraction(ToolTypes.hammer, 1, Direction.WEST, 6, 10, 2, 9,
                    EnumHammerPlate.west.prop, false, (world, pos, blockState, player, hand, hitFace) ->
                    reconfigure(world, pos, blockState, player, Direction.WEST))
    };

    public HammerBaseBlock() {
        super(ForgedBlockCommon.propertiesSolid);

        setRegistryName(unlocalizedName);

        hasItem = true;

        setDefaultState(getDefaultState()
                .with(EnumHammerPlate.east.prop, false)
                .with(EnumHammerPlate.west.prop, false)
                .with(propCell1, false)
                .with(propCell1Charged, false)
                .with(propCell2, false)
                .with(propCell2Charged, false));
    }

    @Override
    public void clientInit() {
        RenderTypeLookup.setRenderLayer(this, RenderType.getCutout());
    }

    @Override
    public void addInformation(final ItemStack stack, @Nullable final IBlockReader world, final List<ITextComponent> tooltip, final ITooltipFlag advanced) {
        tooltip.add(locationTooltip);
        tooltip.add(new StringTextComponent(" "));
        tooltip.add(new TranslationTextComponent("block.multiblock_hint.1x2x1")
                .mergeStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
    }

    public boolean isFueled(World world, BlockPos pos) {
        return TileEntityOptional.from(world, pos, HammerBaseTile.class)
                .map(HammerBaseTile::isFueled)
                .orElse(false);
    }

    public void consumeFuel(World world, BlockPos pos) {
        TileEntityOptional.from(world, pos, HammerBaseTile.class)
                .ifPresent(HammerBaseTile::consumeFuel);
    }

    public void applyEffects(World world, BlockPos pos, ItemStack itemStack, PlayerEntity player) {
        if (hasEffect(world, world.getBlockState(pos), EnumHammerEffect.DAMAGING) && itemStack.getItem() instanceof ModularItem) {
            ModularItem item = (ModularItem) itemStack.getItem();
            int damage = (int) (itemStack.getMaxDamage() * 0.1);
            item.applyDamage(damage, itemStack, player);
        }
    }

    public int getHammerLevel(World world, BlockPos pos) {
        return TileEntityOptional.from(world, pos, HammerBaseTile.class)
                .map(HammerBaseTile::getHammerLevel)
                .orElse(0);
    }

    public static boolean removePlate(World world, BlockPos pos, BlockState blockState, PlayerEntity player, Hand hand, EnumHammerPlate plate) {
        if (!world.isRemote) {
            BlockInteraction.dropLoot(plateLootTable, player, hand, (ServerWorld) world, blockState);
        }

        world.setBlockState(pos, blockState.with(plate.prop, false), 3);

        world.playSound(player, pos, SoundEvents.ITEM_SHIELD_BREAK, SoundCategory.PLAYERS, 1, 0.5f);

        return true;
    }

    public static boolean reconfigure(World world, BlockPos pos, BlockState blockState, PlayerEntity player, Direction face) {
        TileEntityOptional.from(world, pos, HammerBaseTile.class)
                .ifPresent(te -> {
                    if (Direction.EAST.equals(face)) {
                        EnumHammerConfig newConfig = EnumHammerConfig.getNextConfiguration(blockState.get(EnumHammerConfig.eastProp));
                        world.setBlockState(pos, blockState.with(EnumHammerConfig.eastProp, newConfig), 3);

                        te.applyReconfigurationEffect();
                    } else if (Direction.WEST.equals(face)) {
                        EnumHammerConfig newConfig = EnumHammerConfig.getNextConfiguration(blockState.get(EnumHammerConfig.westProp));
                        world.setBlockState(pos, blockState.with(EnumHammerConfig.westProp, newConfig), 3);

                        te.applyReconfigurationEffect();
                    }

                    world.playSound(player, pos, SoundEvents.BLOCK_ANVIL_HIT, SoundCategory.PLAYERS, 1, 1);
        });

        return true;
    }

    public static boolean hasEffect(World world, BlockState blockState, EnumHammerEffect effect) {
        if (effect.requiresBoth) {
            return effect.equals(EnumHammerEffect.fromConfig(blockState.get(EnumHammerConfig.eastProp), 0))
                    && effect.equals(EnumHammerEffect.fromConfig(blockState.get(EnumHammerConfig.westProp), 0));
        }
        return effect.equals(EnumHammerEffect.fromConfig(blockState.get(EnumHammerConfig.eastProp), 0))
                || effect.equals(EnumHammerEffect.fromConfig(blockState.get(EnumHammerConfig.westProp), 0));
    }

    @Override
    public ActionResultType onBlockActivated(final BlockState blockState, final World world, final BlockPos pos, final PlayerEntity player, final Hand hand,
            final BlockRayTraceResult rayTraceResult) {
        Direction blockFacing = blockState.get(propFacing);
        HammerBaseTile te = TileEntityOptional.from(world, pos, HammerBaseTile.class).orElse(null);
        ItemStack heldStack = player.getHeldItem(hand);
        Direction facing = rayTraceResult.getFace();

        if (te == null) {
            return ActionResultType.FAIL;
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

                if (!player.world.isRemote) {
                    BlockUseCriterion.trigger((ServerPlayerEntity) player, world.getBlockState(pos), ItemStack.EMPTY);
                }

                return ActionResultType.SUCCESS;
            } else if (heldStack.getItem() instanceof ItemCellMagmatic) {
                te.putCellInSlot(heldStack, slotIndex);
                player.setHeldItem(hand, ItemStack.EMPTY);
                world.playSound(player, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.PLAYERS, 0.5f, 0.5f);

                if (!player.world.isRemote) {
                    BlockUseCriterion.trigger((ServerPlayerEntity) player, world.getBlockState(pos), heldStack);
                }

                return ActionResultType.SUCCESS;
            }
        } else if (heldStack.getItem() instanceof ItemVentPlate) {
            if (Rotation.CLOCKWISE_90.rotate(blockFacing).equals(facing) && !blockState.get(EnumHammerPlate.east.prop)) {
                world.setBlockState(pos, blockState.with(EnumHammerPlate.east.prop, true), 3);

                if (!player.world.isRemote) {
                    BlockUseCriterion.trigger((ServerPlayerEntity) player, world.getBlockState(pos), heldStack);
                }

                heldStack.shrink(1);

                return ActionResultType.SUCCESS;
            } else if (Rotation.COUNTERCLOCKWISE_90.rotate(blockFacing).equals(facing) && !blockState.get(EnumHammerPlate.west.prop)) {
                world.setBlockState(pos, blockState.with(EnumHammerPlate.west.prop, true), 3);

                if (!player.world.isRemote) {
                    BlockUseCriterion.trigger((ServerPlayerEntity) player, world.getBlockState(pos), heldStack);
                }

                heldStack.shrink(1);

                return ActionResultType.SUCCESS;
            }
        }

        return BlockInteraction.attemptInteraction(world, world.getBlockState(pos), pos, player, hand, rayTraceResult);
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!equals(newState.getBlock())) {
            TileEntityOptional.from(world, pos, HammerBaseTile.class)
                    .ifPresent(tile -> {
                        for (int i = 0; i < 2; i++) {
                            if (tile.hasCellInSlot(i)) {
                                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), tile.getStackInSlot(i).copy());
                            }
                        }
                    });

            TileEntityOptional.from(world, pos, HammerBaseTile.class).ifPresent(TileEntity::remove);
        }
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(final BlockState state, final Direction face, final Collection<ToolType> tools) {
        return Arrays.stream(interactions)
                .filter(interaction -> interaction.isPotentialInteraction(state, state.get(propFacing), face, tools))
                .toArray(BlockInteraction[]::new);
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
                EnumHammerPlate.east.prop, EnumHammerPlate.west.prop, EnumHammerConfig.eastProp, EnumHammerConfig.westProp);
    }

    @Override
    public BlockState updatePostPlacement(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos, BlockPos facingPos) {
        if (Direction.DOWN.equals(facing) && !HammerHeadBlock.instance.equals(facingState.getBlock())) {
            return Blocks.AIR.getDefaultState();
        }

        return super.updatePostPlacement(state, facing, facingState, world, currentPos, facingPos);
    }

    // based on same method implementation in BedBlock
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockState headState = HammerHeadBlock.instance.getDefaultState()
                .with(WATERLOGGED, world.getFluidState(pos.down()).getFluid() == WATER);
        world.setBlockState(pos.down(), headState, 3);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context) {
        if (context.getWorld().getBlockState(context.getPos().down()).isReplaceable(context)) {
            return this.getDefaultState().with(propFacing, context.getPlacementHorizontalFacing().getOpposite());
        }

        // returning null here stops the block from being placed
        return null;
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
