package se.mickelus.tetra.blocks.forged.hammer;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
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
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.blocks.TetraWaterloggedBlock;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IInteractiveBlock;
import se.mickelus.tetra.blocks.salvage.TileBlockInteraction;
import se.mickelus.tetra.util.CastOptional;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static se.mickelus.tetra.blocks.forged.ForgedBlockCommon.locationTooltip;

public class HammerHeadBlock extends TetraWaterloggedBlock implements IInteractiveBlock {
    static final BlockInteraction[] interactions = new BlockInteraction[] {
            new TileBlockInteraction<>(ToolTypes.hammer, 4, Direction.EAST, 1, 11, 7, 11,
                    HammerHeadTile.class, HammerHeadTile::isJammed,
                    (world, pos, blockState, player, hand, hitFace) -> unjam(world, pos, player))
    };

    public static final String unlocalizedName = "hammer_head";

    public static final VoxelShape shape = makeCuboidShape(2, 14, 2, 14, 16, 14);
    public static final VoxelShape jamShape = makeCuboidShape(2, 4, 2, 14, 16, 14);

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static HammerHeadBlock instance;

    public HammerHeadBlock() {
        super(ForgedBlockCommon.propertiesNotSolid);

        setRegistryName(unlocalizedName);

        hasItem = false;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void clientInit() {
        ClientRegistry.bindTileEntityRenderer(HammerHeadTile.type, HammerHeadTESR::new);
    }

    @Override
    public void addInformation(final ItemStack stack, @Nullable final IBlockReader world, final List<ITextComponent> tooltip,
            final ITooltipFlag advanced) {
        tooltip.add(locationTooltip);
    }

    private boolean isJammed(IBlockReader world, BlockPos pos) {
        return TileEntityOptional.from(world, pos, HammerHeadTile.class).map(HammerHeadTile::isJammed).orElse(false);
    }

    private static boolean unjam(World world, BlockPos pos, PlayerEntity playerEntity) {
        TileEntityOptional.from(world, pos, HammerHeadTile.class).ifPresent(tile -> tile.setJammed(false));
        world.playSound(playerEntity, pos, SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.PLAYERS, 1, 0.5f);
        return true;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        return BlockInteraction.attemptInteraction(world, state, pos, player, hand, hit);
    }

    private boolean isFunctional(World world, BlockPos pos) {
        BlockPos basePos = pos.up();
        boolean functionalBase = CastOptional.cast(world.getBlockState(basePos).getBlock(), HammerBaseBlock.class)
                .map(base -> base.isFunctional(world, basePos))
                .orElse(false);

        return functionalBase && !isJammed(world, pos);
    }

    @Override
    public boolean canProvideTools(World world, BlockPos pos, BlockPos targetPos) {
        return pos.equals(targetPos.up());
    }

    @Override
    public Collection<ToolType> getTools(World world, BlockPos pos, BlockState blockState) {
        if (isFunctional(world, pos)) {
            return Collections.singletonList(ToolTypes.hammer);
        }
        return super.getTools(world, pos, blockState);
    }

    @Override
    public int getToolLevel(World world, BlockPos pos, BlockState blockState, ToolType toolType) {
        if (ToolTypes.hammer.equals(toolType) && isFunctional(world, pos)) {
            BlockPos basePos = pos.up();
            HammerBaseBlock baseBlock = (HammerBaseBlock) world.getBlockState(basePos).getBlock();
            return baseBlock.getHammerLevel(world, basePos);
        }
        return super.getToolLevel(world, pos, blockState, toolType);
    }

    @Override
    public ItemStack onCraftConsumeTool(World world, BlockPos pos, BlockState blockState, ItemStack targetStack, String slot, boolean isReplacing, PlayerEntity player,
            ToolType requiredTool, int requiredLevel, boolean consumeResources) {
        BlockPos basePos = pos.up();
        BlockState baseState = world.getBlockState(basePos);
        ItemStack upgradedStack = CastOptional.cast(baseState.getBlock(), HammerBaseBlock.class)
                .map(base -> base.applyCraftEffects(world, basePos, baseState, targetStack, slot, isReplacing, player, requiredTool, requiredLevel, consumeResources))
                .orElse(targetStack);

        if (consumeResources) {
            TileEntityOptional.from(world, pos, HammerHeadTile.class).ifPresent(HammerHeadTile::activate);
            world.playSound(player, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.5f, (float) (0.5 + Math.random() * 0.2));
        }

        return upgradedStack;
    }

    @Override
    public void animateTick(BlockState state, World world, BlockPos pos, Random rand) {
        if (isJammed(world, pos) && rand.nextBoolean()) {
            boolean flipped = rand.nextBoolean();
            float x = pos.getX() + (flipped ? rand.nextBoolean() ? 0.1f : 0.9f : rand.nextFloat());
            float z = pos.getZ() + (!flipped ? rand.nextBoolean() ? 0.1f : 0.9f : rand.nextFloat());
            world.addParticle(new BlockParticleData(ParticleTypes.FALLING_DUST, state), x, pos.getY() + 1, z, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public ItemStack onActionConsumeTool(World world, BlockPos pos, BlockState blockState, ItemStack targetStack, PlayerEntity player,
            ToolType requiredTool, int requiredLevel, boolean consumeResources) {
        BlockPos basePos = pos.up();
        BlockState baseState = world.getBlockState(basePos);
        ItemStack upgradedStack = CastOptional.cast(baseState.getBlock(), HammerBaseBlock.class)
                .map(base -> base.applyActionEffects(world, basePos, baseState, targetStack, player, requiredTool, requiredLevel, consumeResources))
                .orElse(targetStack);

        if (consumeResources) {
            TileEntityOptional.from(world, pos, HammerHeadTile.class).ifPresent(HammerHeadTile::activate);
            world.playSound(player, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.5f, (float) (0.5 + Math.random() * 0.2));
        }
        return upgradedStack;
    }

    @Override
    public BlockState updatePostPlacement(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos, BlockPos facingPos) {
        if (Direction.UP.equals(facing) && !HammerBaseBlock.instance.equals(facingState.getBlock())) {
            return state.get(BlockStateProperties.WATERLOGGED) ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState();
        }

        return super.updatePostPlacement(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    public VoxelShape getShape(final BlockState blockState, final IBlockReader world, final BlockPos pos, final ISelectionContext context) {
        if (context == ISelectionContext.dummy()) {
            return jamShape;
        } else if (isJammed(world, pos)) {
            return jamShape;
        }
        return shape;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world) {
        return new HammerHeadTile();
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(World world, BlockPos pos, BlockState blockState, Direction face, Collection<ToolType> tools) {
        if (isJammed(world, pos) && face.getAxis().getPlane() == Direction.Plane.HORIZONTAL) {
            return interactions;
        }
        return new BlockInteraction[0];
    }
}
