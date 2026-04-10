package se.mickelus.tetra.blocks.holo;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.RegistryObject;
import se.mickelus.mutil.network.PacketHandler;
import se.mickelus.mutil.util.RotationHelper;
import se.mickelus.tetra.TetraItemAbilities;
import se.mickelus.tetra.advancements.BlockUseCriterion;
import se.mickelus.tetra.blocks.TetraWaterloggedBlock;
import se.mickelus.tetra.interactions.SecondaryInteractionHandler;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.items.modular.impl.holo.ModularHolosphereItem;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static se.mickelus.tetra.util.ItemStackTagHelper.getTag;
import static se.mickelus.tetra.util.ItemStackTagHelper.hasTag;

public class HolosphereBlock extends TetraWaterloggedBlock implements EntityBlock {
    public static final String identifier = "holosphere";
    private static final VoxelShape shape = Block.box(5.5, 0, 5.5, 10.5, 5, 10.5);
    public static RegistryObject<HolosphereBlock> instance;

    public HolosphereBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(0, 20.0F)
                .sound(SoundType.DEEPSLATE));
    }

    public static InteractionResult place(BlockPlaceContext context) {
        ItemStack itemstack = context.getItemInHand();
        if (context.canPlace() && hasTag(itemstack)) {
            Block block = instance.get();
            BlockState blockState = block.defaultBlockState();
            boolean couldPlace = context.getLevel().setBlock(context.getClickedPos(), blockState, 11);
            if (couldPlace) {
                BlockPos pos = context.getClickedPos();
                Level level = context.getLevel();
                Player player = context.getPlayer();
                BlockState placedBlockState = level.getBlockState(pos);
                if (placedBlockState.is(blockState.getBlock())) {
                    level.getBlockEntity(pos, HolosphereBlockEntity.type.get())
                            .ifPresent(blockEntity -> blockEntity.setItemTag(getTag(itemstack)));
                    placedBlockState.getBlock().setPlacedBy(level, pos, placedBlockState, player, itemstack);
                    if (player instanceof ServerPlayer) {
                        CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, pos, itemstack);
                    }
                }

                level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, placedBlockState));
                SoundType soundtype = placedBlockState.getSoundType(level, pos, context.getPlayer());
                level.playSound(player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F,
                        soundtype.getPitch() * 0.8F);
                if (player == null || !player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }

                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.FAIL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    public void commonInit(PacketHandler packetHandler) {
        SecondaryInteractionHandler.registerInteraction(new ToggleScanModeInteraction("scan_toggle_on", true));
        SecondaryInteractionHandler.registerInteraction(new ToggleScanModeInteraction("scan_toggle_off", false));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new HolosphereBlockEntity(p_153215_, p_153216_);
    }

    private InteractionResult useInternal(BlockState blockState, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (world.getBlockEntity(pos) instanceof HolosphereBlockEntity entity
                && entity.inScanMode()
                && itemStack.getItem() instanceof ItemModularHandheld item) {
            int level = item.getToolLevel(itemStack, TetraItemAbilities.hammer);
            if (level > 0) {
                boolean canSwing = player.getAttackStrengthScale(0) > 0.8f;
                if (!world.isClientSide() && canSwing) {
                    float angle = (float) RotationHelper.getHorizontalAngle(Vec3.atBottomCenterOf(pos), player.position());
                    entity.use(level, item.getToolEfficiency(itemStack, TetraItemAbilities.hammer), angle);

                    Map<String, String> data = new HashMap<>();
                    data.put("percussion_scan", "true");
                    BlockUseCriterion.trigger((ServerPlayer) player, blockState, itemStack, data);
                }

                if (canSwing) {
                    item.tickProgression(player, itemStack, 2);
                    item.applyDamage(2, itemStack, player);
                    world.playSound(player, pos, SoundEvents.NETHERITE_BLOCK_HIT, SoundSource.PLAYERS, 0.3f, 1f + 0.5f * (float) Math.random());
                } else {
                    world.playSound(player, pos, SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.2f, 0.5f);
                }

                player.resetAttackStrengthTicker();

                return InteractionResult.sidedSuccess(canSwing);
            }
        }

        if (world.isClientSide()) {
            ModularHolosphereItem.showGui();
        } else {
            Map<String, String> data = new HashMap<>();
            data.put("holosphere_open", "true");
            BlockUseCriterion.trigger((ServerPlayer) player, blockState, itemStack, data);
        }

        return InteractionResult.sidedSuccess(world.isClientSide());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState blockState, Level world, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        return switch (useInternal(blockState, world, pos, player, hand, hit)) {
            case SUCCESS, CONSUME -> ItemInteractionResult.sidedSuccess(world.isClientSide);
            case CONSUME_PARTIAL -> ItemInteractionResult.CONSUME_PARTIAL;
            case FAIL -> ItemInteractionResult.FAIL;
            default -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        return useInternal(blockState, world, pos, player, InteractionHand.MAIN_HAND, hit);
    }

    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        BlockState result = super.playerWillDestroy(world, pos, state, player);

        if (!world.isClientSide && !player.isCreative() && world.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            world.getBlockEntity(pos, HolosphereBlockEntity.type.get())
                    .ifPresent(blockEntity -> {
                        ItemStack itemStack = blockEntity.getItemStack();
                        ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, itemStack);
                        itemEntity.setDefaultPickUpDelay();
                        world.addFreshEntity(itemEntity);
                    });
        }

        return result;
    }
}
