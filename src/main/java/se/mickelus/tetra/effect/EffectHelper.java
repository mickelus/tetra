package se.mickelus.tetra.effect;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.apache.commons.lang3.mutable.MutableBoolean;
import se.mickelus.tetra.items.modular.IModularItem;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@ParametersAreNonnullByDefault
public class EffectHelper {
    private static final Cache<UUID, Float> cooledAttackStrengthCache = CacheBuilder.newBuilder()
            .maximumSize(50)
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .build();
    private static final Cache<UUID, Boolean> sprintingCache = CacheBuilder.newBuilder()
            .maximumSize(50)
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .build();

    public static void setCooledAttackStrength(Player player, float strength) {
        cooledAttackStrengthCache.put(player.getUUID(), strength);
    }

    public static float getCooledAttackStrength(Player player) {
        try {
            return cooledAttackStrengthCache.get(player.getUUID(), () -> 0f);
        } catch (ExecutionException e) {
            return 0;
        }
    }

    public static void setSprinting(LivingEntity player, boolean isSprinting) {
        sprintingCache.put(player.getUUID(), isSprinting);
    }

    public static boolean getSprinting(LivingEntity player) {
        try {
            return sprintingCache.get(player.getUUID(), () -> false);
        } catch (ExecutionException e) {
            return false;
        }
    }


    public static int getEffectLevel(ItemStack itemStack, ItemEffect effect) {
        IModularItem item = (IModularItem) itemStack.getItem();
        return item.getEffectLevel(itemStack, effect);
    }

    public static float getEffectEfficiency(ItemStack itemStack, ItemEffect effect) {
        IModularItem item = (IModularItem) itemStack.getItem();
        return item.getEffectEfficiency(itemStack, effect);
    }

    /**
     * Break a block in the world, as a player.
     * Based on how players break blocks in vanilla {@link net.minecraft.server.management.PlayerInteractionManager#tryHarvestBlock}, but allows
     * control over the used itemstack and without causing damage and honing progression for the used itemstack
     *
     * @param world          the world in which to break blocks
     * @param breakingPlayer the player which is breaking the blocks
     * @param toolStack      the itemstack used to break the blocks
     * @param pos            the position which to break blocks around
     * @param blockState     the state of the block that is to broken
     * @param harvest        true if the player is ment to harvest the block, false if it should just magically disappear
     * @param tryReplant     attempts to replant crop blocks when true
     * @return True if the player was allowed to break the block, otherwise false
     */
    public static boolean breakBlock(Level world, Player breakingPlayer, ItemStack toolStack, BlockPos pos, BlockState blockState,
            boolean harvest, boolean tryReplant) {
        if (!world.isClientSide) {
            ServerLevel serverWorld = (ServerLevel) world;
            ServerPlayer serverPlayer = (ServerPlayer) breakingPlayer;
            GameType gameType = serverPlayer.gameMode.getGameModeForPlayer();

            int exp = net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(world, gameType, serverPlayer, pos);

            BlockEntity tileEntity = world.getBlockEntity(pos);

            if (exp != -1) {
                boolean canRemove = !toolStack.onBlockStartBreak(pos, breakingPlayer)
                        && !breakingPlayer.blockActionRestricted(world, pos, gameType)
                        && (!harvest || blockState.canHarvestBlock(world, pos, breakingPlayer))
                        && blockState.getBlock().onDestroyedByPlayer(blockState, world, pos, breakingPlayer, harvest, world.getFluidState(pos));

                if (canRemove) {
                    blockState.getBlock().destroy(world, pos, blockState);

                    if (tryReplant) {
                        breakAndReplant(serverWorld, pos, blockState, breakingPlayer, toolStack, harvest);
                    } else if (harvest) {
                        blockState.getBlock().playerDestroy(world, breakingPlayer, pos, blockState, tileEntity, toolStack);
                    }

                    if (harvest && exp > 0) {
                        blockState.getBlock().popExperience(serverWorld, pos, exp);
                    }

                    if (harvest) {
                        blockState.spawnAfterBreak(serverWorld, pos, toolStack, false);
                    }
                }
                return canRemove;
            }

            return false;
        } else {
            return blockState.getBlock().onDestroyedByPlayer(blockState, world, pos, breakingPlayer, harvest,
                    world.getFluidState(pos));
        }
    }

    public static boolean tryReplant(ItemStack itemStack, ToolAction toolAction) {
        return toolAction == ToolActions.HOE_DIG && EnchantmentHelper.hasSilkTouch(itemStack);
    }

    private static boolean breakAndReplant(ServerLevel serverLevel, BlockPos pos, BlockState blockState, Player entity, ItemStack itemStack, boolean doDrops) {
        BlockState newBlock = blockState.getBlock() instanceof CropBlock crop ? crop.defaultBlockState() : Blocks.AIR.defaultBlockState();

        MutableBoolean foundSeed = new MutableBoolean(false);
        Item seedItem = blockState.getBlock().asItem();
        Block.getDrops(blockState, serverLevel, pos, serverLevel.getBlockEntity(pos), entity, itemStack).forEach(droppedStack -> {
            if (droppedStack.getItem() == seedItem && !foundSeed.getValue()) {
                droppedStack.shrink(1);
                foundSeed.setValue(true);
            }

            if (doDrops && !droppedStack.isEmpty())
                Block.popResource(serverLevel, pos, droppedStack);
        });

        serverLevel.setBlockAndUpdate(pos, newBlock);

        return foundSeed.getValue();
    }

    /**
     * Sends an event to a specific player.
     *
     * @param player the player the event will be sent to
     * @param type   an integer representation of the event
     * @param pos    the position in which the event takes place
     * @param data   an integer representation of event data (e.g. Block.getStateId)
     */
    public static void sendEventToPlayer(ServerPlayer player, int type, BlockPos pos, int data) {
        player.connection.send(new ClientboundLevelEventPacket(type, pos, data, false));
    }

    /**
     * Variant on {@link EnchantmentHelper#doPostDamageEffects} that allows control over the held itemstack
     *
     * @param itemStack
     * @param target
     * @param attacker
     */
    public static void applyEnchantmentHitEffects(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
        EnchantmentHelper.getEnchantments(itemStack).forEach((enchantment, level) -> enchantment.doPostAttack(attacker, target, level));

        if (attacker != null) {
            for (ItemStack equipment : attacker.getAllSlots()) {
                EnchantmentHelper.getEnchantments(equipment).forEach((enchantment, level) -> enchantment.doPostAttack(attacker, target, level));
            }
        }

        // fire aspect has to be applied separately :o
        int fireAspectLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_ASPECT, itemStack);
        if (fireAspectLevel > 0) {
            target.setSecondsOnFire(fireAspectLevel * 4);
        }
    }

    /**
     * Based on {@link Player#getDigSpeed(BlockState, BlockPos)}, modifies the given efficiency based on player state
     *
     * @param player
     * @param itemStack
     * @param base
     * @param blockState
     * @param pos
     * @return
     */
    public static float getModifiedEfficiency(Player player, ItemStack itemStack, float base, @Nullable BlockState blockState, @Nullable BlockPos pos) {
        float result = base;
        if (result > 1) {
            int efficiencyLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, itemStack);
            result += efficiencyLevel * efficiencyLevel + 1;
        }

        if (MobEffectUtil.hasDigSpeed(player)) {
            result *= 1.0F + (MobEffectUtil.getDigSpeedAmplification(player) + 1) * 0.2F;
        }

        if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            switch (player.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
                case 0:
                    result *= 0.3F;
                    break;
                case 1:
                    result *= 0.09F;
                    break;
                case 2:
                    result *= 0.0027F;
                    break;
                case 3:
                default:
                    result *= 8.1E-4F;
            }
        }

        if (player.isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player)) {
            result /= 5.0F;
        }

        if (!player.onGround()) {
            result /= 5.0F;
        }

        if (blockState != null) {
            result = net.minecraftforge.event.ForgeEventFactory.getBreakSpeed(player, blockState, result, pos);
        }

        return result;
    }
}
