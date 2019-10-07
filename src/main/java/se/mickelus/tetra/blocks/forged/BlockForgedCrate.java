package se.mickelus.tetra.blocks.forged;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.pattern.BlockStateMatcher;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.ITetraBlock;
import se.mickelus.tetra.blocks.Materials;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IBlockCapabilityInteractive;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemEffectHandler;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;


public class BlockForgedCrate extends FallingBlock implements ITetraBlock, IBlockCapabilityInteractive {
    public static final DirectionProperty propFacing = HorizontalBlock.HORIZONTAL_FACING;
    public static final BooleanProperty propStacked = BooleanProperty.create("stacked");
    public static final IntegerProperty propIntegrity = IntegerProperty.create("integrity", 0, 3);

    static final BlockInteraction[] interactions = new BlockInteraction[] {
            new BlockInteraction(Capability.pry, 1, Direction.EAST, 6, 8, 6, 8,
                    BlockStateMatcher.ANY,
                    BlockForgedCrate::attemptBreakPry),
            new BlockInteraction(Capability.hammer, 3, Direction.EAST, 1, 4, 1, 4,
                    BlockStateMatcher.ANY,
                    BlockForgedCrate::attemptBreakHammer),
            new BlockInteraction(Capability.hammer, 3, Direction.EAST, 10, 13, 10, 13,
                    BlockStateMatcher.ANY,
                    BlockForgedCrate::attemptBreakHammer),
    };

    static final String unlocalizedName = "forged_crate";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockForgedCrate instance;

    public static final ResourceLocation crateLootTable = new ResourceLocation(TetraMod.MOD_ID, "forged/crate_break");

    public BlockForgedCrate() {
        super(Block.Properties.create(Materials.forgedCrate)
                .hardnessAndResistance(10));


        this.setDefaultState(getStateContainer().getBaseState()
                .with(propFacing, Direction.EAST)
                .with(propStacked, false)
                .with(propIntegrity, 3));
    }

    private static boolean attemptBreakHammer(World world, BlockPos pos, BlockState blockState, PlayerEntity player, Hand hand, Direction facing) {
        return attemptBreak(world, pos, blockState, player, player.getHeldItem(hand), Capability.hammer, 2, 1);
    }

    private static boolean attemptBreakPry(World world, BlockPos pos, BlockState blockState, PlayerEntity player, Hand hand, Direction facing) {
        return attemptBreak(world, pos, blockState, player, player.getHeldItem(hand), Capability.pry, 0, 2);
    }

    private static boolean attemptBreak(World world, BlockPos pos, BlockState blockState, PlayerEntity player, ItemStack itemStack,
            Capability capability, int min, int multiplier) {

        int integrity = blockState.get(propIntegrity);

        int progress = CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .map(item -> item.getCapabilityLevel(itemStack, capability))
                .map(level -> ( level - min ) * multiplier)
                .orElse(1);

        if (integrity - progress >= 0) {
            if (Capability.hammer.equals(capability)) {
                world.playSound(player, pos, SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.PLAYERS, 1, 0.5f);
            } else {
                world.playSound(player, pos, SoundEvents.BLOCK_LADDER_STEP, SoundCategory.PLAYERS, 0.7f, 2f);
            }

            world.setBlockState(pos, blockState.with(propIntegrity, integrity - progress));
        } else {
            world.playEvent(player, 2001, pos, Block.getStateId(blockState));
            ItemEffectHandler.breakBlock(world, player, itemStack, pos, blockState);
        }

        return true;
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(BlockState state, Direction face, Collection<Capability> capabilities) {
            return interactions;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTrace) {
        Vec3d hitVector = rayTrace.getHitVec();
        return BlockInteraction.attemptInteraction(world, state.getExtendedState(world, pos), pos, player, hand, rayTrace.getFace(),
                hitVector.x, hitVector.y, hitVector.z);
    }

    @Override
    public ResourceLocation getLootTable() {
        // todo 1.14: changed to new drops setup, check if crates properly drop loot
        return crateLootTable;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced) {
        tooltip.add(new TranslationTextComponent("forged_description").setStyle(new Style().setColor(TextFormatting.DARK_GRAY)));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getDefaultState().with(propFacing, context.getPlacementHorizontalFacing());
    }

    @Override
    public BlockState getExtendedState(BlockState state, IBlockReader world, BlockPos pos) {
        if (equals(world.getBlockState(pos.down()).getBlock())) {
            return super.getExtendedState(state, world, pos).with(propStacked, true);
        }
        return super.getExtendedState(state, world, pos);
    }

    @Override
    public BlockState rotate(BlockState state, IWorld world, BlockPos pos, Rotation direction) {
        return state.with(propFacing, direction.rotate(state.get(propFacing)));
    }

    @Override
    public boolean causesSuffocation(BlockState blockState, IBlockReader world, BlockPos pos) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState blockState, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        Vec3d facingOffset = new Vec3d(blockState.get(propFacing).getDirectionVec()).scale(0.0625);
        VoxelShape shape = Block.makeCuboidShape(0.0625, 0, 0.0625, 0.9375, 0.875, 0.9375)
                .withOffset(facingOffset.x, facingOffset.y, facingOffset.z);

        if (getExtendedState(blockState, world, pos).get(propStacked)) {
            return shape.withOffset(0, -0.125, 0);
        }

        return shape;
    }

    @Override
    public boolean hasItem() {
        return true;
    }

    @Override
    public void registerItem(IForgeRegistry<Item> registry) {
        registerItem(registry, this);
    }
}
