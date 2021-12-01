package se.mickelus.tetra.blocks.forged.hammer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
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
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static se.mickelus.tetra.blocks.forged.ForgedBlockCommon.locationTooltip;
@ParametersAreNonnullByDefault
public class HammerHeadBlock extends TetraWaterloggedBlock implements IInteractiveBlock, EntityBlock {
    static final BlockInteraction[] interactions = new BlockInteraction[] {
            new TileBlockInteraction<>(ToolTypes.hammer, 4, Direction.EAST, 1, 11, 7, 11,
                    HammerHeadTile.class, HammerHeadTile::isJammed,
                    (world, pos, blockState, player, hand, hitFace) -> unjam(world, pos, player))
    };

    public static final String unlocalizedName = "hammer_head";

    public static final VoxelShape shape = box(2, 14, 2, 14, 16, 14);
    public static final VoxelShape jamShape = box(2, 4, 2, 14, 16, 14);

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
    public void appendHoverText(final ItemStack stack, @Nullable final BlockGetter world, final List<Component> tooltip,
            final TooltipFlag advanced) {
        tooltip.add(locationTooltip);
    }

    private boolean isJammed(BlockGetter world, BlockPos pos) {
        return TileEntityOptional.from(world, pos, HammerHeadTile.class).map(HammerHeadTile::isJammed).orElse(false);
    }

    private static boolean unjam(Level world, BlockPos pos, Player playerEntity) {
        TileEntityOptional.from(world, pos, HammerHeadTile.class).ifPresent(tile -> tile.setJammed(false));
        world.playSound(playerEntity, pos, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.PLAYERS, 1, 0.5f);
        return true;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return BlockInteraction.attemptInteraction(world, state, pos, player, hand, hit);
    }

    private boolean isFunctional(Level world, BlockPos pos) {
        BlockPos basePos = pos.above();
        boolean functionalBase = CastOptional.cast(world.getBlockState(basePos).getBlock(), HammerBaseBlock.class)
                .map(base -> base.isFunctional(world, basePos))
                .orElse(false);

        return functionalBase && !isJammed(world, pos);
    }

    @Override
    public boolean canProvideTools(Level world, BlockPos pos, BlockPos targetPos) {
        return pos.equals(targetPos.above());
    }

    @Override
    public Collection<ToolType> getTools(Level world, BlockPos pos, BlockState blockState) {
        if (isFunctional(world, pos)) {
            return Collections.singletonList(ToolTypes.hammer);
        }
        return super.getTools(world, pos, blockState);
    }

    @Override
    public int getToolLevel(Level world, BlockPos pos, BlockState blockState, ToolType toolType) {
        if (ToolTypes.hammer.equals(toolType) && isFunctional(world, pos)) {
            BlockPos basePos = pos.above();
            HammerBaseBlock baseBlock = (HammerBaseBlock) world.getBlockState(basePos).getBlock();
            return baseBlock.getHammerLevel(world, basePos);
        }
        return super.getToolLevel(world, pos, blockState, toolType);
    }

    @Override
    public ItemStack onCraftConsumeTool(Level world, BlockPos pos, BlockState blockState, ItemStack targetStack, String slot, boolean isReplacing, Player player,
            ToolType requiredTool, int requiredLevel, boolean consumeResources) {
        BlockPos basePos = pos.above();
        BlockState baseState = world.getBlockState(basePos);
        ItemStack upgradedStack = CastOptional.cast(baseState.getBlock(), HammerBaseBlock.class)
                .map(base -> base.applyCraftEffects(world, basePos, baseState, targetStack, slot, isReplacing, player, requiredTool, requiredLevel, consumeResources))
                .orElse(targetStack);

        if (consumeResources) {
            TileEntityOptional.from(world, pos, HammerHeadTile.class).ifPresent(HammerHeadTile::activate);
            world.playSound(player, pos, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.2f, (float) (0.5 + Math.random() * 0.2));
        }

        return upgradedStack;
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, Random rand) {
        if (isJammed(world, pos) && rand.nextBoolean()) {
            boolean flipped = rand.nextBoolean();
            float x = pos.getX() + (flipped ? rand.nextBoolean() ? 0.1f : 0.9f : rand.nextFloat());
            float z = pos.getZ() + (!flipped ? rand.nextBoolean() ? 0.1f : 0.9f : rand.nextFloat());
            world.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, state), x, pos.getY() + 1, z, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public ItemStack onActionConsumeTool(Level world, BlockPos pos, BlockState blockState, ItemStack targetStack, Player player,
            ToolType requiredTool, int requiredLevel, boolean consumeResources) {
        BlockPos basePos = pos.above();
        BlockState baseState = world.getBlockState(basePos);
        ItemStack upgradedStack = CastOptional.cast(baseState.getBlock(), HammerBaseBlock.class)
                .map(base -> base.applyActionEffects(world, basePos, baseState, targetStack, player, requiredTool, requiredLevel, consumeResources))
                .orElse(targetStack);

        if (consumeResources) {
            TileEntityOptional.from(world, pos, HammerHeadTile.class).ifPresent(HammerHeadTile::activate);
            world.playSound(player, pos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.2f, (float) (0.5 + Math.random() * 0.2));
        }
        return upgradedStack;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
        if (Direction.UP.equals(facing) && !HammerBaseBlock.instance.equals(facingState.getBlock())) {
            return state.getValue(BlockStateProperties.WATERLOGGED) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    public VoxelShape getShape(final BlockState blockState, final BlockGetter world, final BlockPos pos, final CollisionContext context) {
        if (context == CollisionContext.empty()) {
            return jamShape;
        } else if (isJammed(world, pos)) {
            return jamShape;
        }
        return shape;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(Level world, BlockPos pos, BlockState blockState, Direction face, Collection<ToolType> tools) {
        if (isJammed(world, pos) && face.getAxis().getPlane() == Direction.Plane.HORIZONTAL) {
            return interactions;
        }
        return new BlockInteraction[0];
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new HammerHeadTile(p_153215_, p_153216_);
    }
}
