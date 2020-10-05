package se.mickelus.tetra.blocks.forged.hammer;

import net.minecraft.block.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.advancements.BlockUseCriterion;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IInteractiveBlock;
import se.mickelus.tetra.blocks.salvage.InteractiveBlockOverlay;
import se.mickelus.tetra.blocks.salvage.TileBlockInteraction;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

import static net.minecraft.fluid.Fluids.WATER;
import static net.minecraft.state.properties.BlockStateProperties.WATERLOGGED;
import static se.mickelus.tetra.blocks.forged.ForgedBlockCommon.locationTooltip;

public class HammerBaseBlock extends TetraBlock implements IInteractiveBlock {
    public static final DirectionProperty facingProp = HorizontalBlock.HORIZONTAL_FACING;

    public static final String qualityImprovementKey = "quality";

    public static final String unlocalizedName = "hammer_base";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static HammerBaseBlock instance;

    public static final BlockInteraction[] interactions = new BlockInteraction[] {
            new TileBlockInteraction<>(ToolTypes.pry, 1, Direction.EAST, 5, 11, 10, 12,
                    HammerBaseTile.class, tile -> tile.getEffect(true) != null,
                    (world, pos, blockState, player, hand, hitFace) -> removeModule(world, pos, blockState, player, hand, hitFace, true)),
            new TileBlockInteraction<>(ToolTypes.pry, 1, Direction.WEST, 5, 11, 10, 12,
                    HammerBaseTile.class, tile -> tile.getEffect(false) != null,
                    (world, pos, blockState, player, hand, hitFace) -> removeModule(world, pos, blockState, player, hand, hitFace, false))
    };

    public HammerBaseBlock() {
        super(ForgedBlockCommon.propertiesNotSolid);

        setRegistryName(unlocalizedName);

        hasItem = true;
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder) {
        builder.add(facingProp);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void clientInit() {
        ClientRegistry.bindTileEntityRenderer(HammerBaseTile.type, HammerBaseRenderer::new);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void addInformation(final ItemStack stack, @Nullable final IBlockReader world, final List<ITextComponent> tooltip, final ITooltipFlag advanced) {
        tooltip.add(locationTooltip);
        tooltip.add(new StringTextComponent(" "));
        tooltip.add(new TranslationTextComponent("block.multiblock_hint.1x2x1")
                .mergeStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
    }

    public boolean isFunctional(World world, BlockPos pos) {
        return TileEntityOptional.from(world, pos, HammerBaseTile.class)
                .map(HammerBaseTile::isFunctional)
                .orElse(false);
    }

    public void consumeFuel(World world, BlockPos pos) {
        TileEntityOptional.from(world, pos, HammerBaseTile.class)
                .ifPresent(HammerBaseTile::consumeFuel);
    }

    public int getHammerLevel(World world, BlockPos pos) {
        return TileEntityOptional.from(world, pos, HammerBaseTile.class)
                .map(HammerBaseTile::getHammerLevel)
                .orElse(0);
    }

    public static boolean removeModule(World world, BlockPos pos, BlockState blockState, PlayerEntity player, Hand hand, Direction hitFace, boolean isA) {
        ItemStack moduleStack = TileEntityOptional.from(world, pos, HammerBaseTile.class)
                .map(te -> te.removeModule(isA))
                .map(ItemStack::new)
                .orElse(null);

        if (moduleStack != null && !world.isRemote) {
            if (player.inventory.addItemStackToInventory(moduleStack)) {
                player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1, 1);
            } else {
                spawnAsEntity(world, pos.offset(hitFace), moduleStack);
            }
        }

        world.playSound(player, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.PLAYERS, 0.5f, 0.6f);

        return true;
    }

    public ItemStack applyCraftEffects(World world, BlockPos pos, BlockState blockState, ItemStack targetStack, String slot, boolean isReplacing,
            PlayerEntity player, ToolType requiredTool, int requiredLevel, boolean consumeResources) {
        if (consumeResources) {
            consumeFuel(world, pos);
        }

        if (isReplacing) {
            int preciseLevel = TileEntityOptional.from(world, pos, HammerBaseTile.class)
                    .map(te -> te.getEffectLevel(HammerEffect.precise))
                    .orElse(0);

            if (preciseLevel > 0) {
                ItemStack upgradedStack = targetStack.copy();

                ItemModuleMajor.addImprovement(upgradedStack, slot, qualityImprovementKey, preciseLevel);
                return upgradedStack;
            }
        }

        return targetStack;
    }

    public ItemStack applyActionEffects(World world, BlockPos pos, BlockState blockState, ItemStack targetStack, PlayerEntity player,
            ToolType requiredTool, int requiredLevel, boolean consumeResources) {
        if (consumeResources) {
            consumeFuel(world, pos);
        }
        return targetStack;
    }

    private Map<String, String> getAdvancementData(World world, BlockPos pos) {
        return TileEntityOptional.from(world, pos, HammerBaseTile.class)
                .map(tile -> {
                    Map<String, String> result = new HashMap<>();
                    result.put("functional", String.valueOf(tile.isFunctional()));

                    Optional.ofNullable(tile.getEffect(true))
                            .ifPresent(module -> result.put("moduleA", module.toString()));
                    Optional.ofNullable(tile.getEffect(false))
                            .ifPresent(module -> result.put("moduleB", module.toString()));

                    return result;
                })
                .orElseGet(Collections::emptyMap);
    }

    @Override
    public ActionResultType onBlockActivated(final BlockState blockState, final World world, final BlockPos pos, final PlayerEntity player, final Hand hand,
            final BlockRayTraceResult rayTraceResult) {
        Direction blockFacing = blockState.get(facingProp);
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
                    spawnAsEntity(world, pos.offset(facing), cell);
                }

                world.playSound(player, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.PLAYERS, 0.5f, 0.6f);

                if (!player.world.isRemote) {
                    BlockUseCriterion.trigger((ServerPlayerEntity) player, world.getBlockState(pos), ItemStack.EMPTY, getAdvancementData(world, pos));
                }

                return ActionResultType.func_233537_a_(player.world.isRemote);
            } else if (heldStack.getItem() instanceof ItemCellMagmatic) {
                te.putCellInSlot(heldStack, slotIndex);
                player.setHeldItem(hand, ItemStack.EMPTY);
                world.playSound(player, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.PLAYERS, 0.5f, 0.5f);

                if (!player.world.isRemote) {
                    BlockUseCriterion.trigger((ServerPlayerEntity) player, world.getBlockState(pos), heldStack, getAdvancementData(world, pos));
                }

                return ActionResultType.func_233537_a_(player.world.isRemote);
            }
        } else {
            boolean isA = Rotation.CLOCKWISE_90.rotate(blockFacing).equals(facing);

            if (te.getEffect(isA) == null) {
                boolean success = te.setModule(isA, heldStack.getItem());
                if (success) {
                    if (!player.world.isRemote) {
                        BlockUseCriterion.trigger((ServerPlayerEntity) player, world.getBlockState(pos), heldStack, getAdvancementData(world, pos));
                    }

                    world.playSound(player, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.PLAYERS, 0.5f, 0.5f);
                    heldStack.shrink(1);

                    if (world.isRemote) {
                        InteractiveBlockOverlay.markDirty();
                    }

                    return ActionResultType.func_233537_a_(player.world.isRemote);
                }
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

                        Stream.of(tile.getEffect(true), tile.getEffect(false))
                                .filter(Objects::nonNull)
                                .map(HammerEffect::getItem)
                                .map(ItemStack::new)
                                .forEach(stack -> InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack));
                    });

            TileEntityOptional.from(world, pos, HammerBaseTile.class).ifPresent(TileEntity::remove);
        }
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(World world, BlockPos pos, final BlockState state, final Direction face, final Collection<ToolType> tools) {
        return Arrays.stream(interactions)
                .filter(interaction -> interaction.isPotentialInteraction(world, pos, state, state.get(facingProp), face, tools))
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
            return this.getDefaultState().with(facingProp, context.getPlacementHorizontalFacing().getOpposite());
        }

        // returning null here stops the block from being placed
        return null;
    }

    @Override
    public BlockState rotate(final BlockState state, final Rotation rotation) {
        return state.with(facingProp, rotation.rotate(state.get(facingProp)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.toRotation(state.get(facingProp)));
    }
}
