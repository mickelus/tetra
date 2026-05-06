package se.mickelus.tetra.effect;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.ItemAbilities;
import org.apache.commons.lang3.mutable.MutableBoolean;
import se.mickelus.tetra.items.modular.IModularItem;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.Objects;

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

    public static Holder<MobEffect> effectHolder(MobEffect effect) {
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);
    }

    public static Holder<Enchantment> enchantmentHolder(ResourceKey<Enchantment> enchantment) {
        return Objects.requireNonNull(CommonHooks.resolveLookup(Registries.ENCHANTMENT), "Enchantment registry lookup unavailable")
                .getOrThrow(enchantment);
    }

    public static int getEnchantmentLevel(ResourceKey<Enchantment> enchantment, ItemStack itemStack) {
        return EnchantmentHelper.getItemEnchantmentLevel(enchantmentHolder(enchantment), itemStack);
    }

    public static int getEnchantmentLevel(ResourceKey<Enchantment> enchantment, LivingEntity entity) {
        return EnchantmentHelper.getEnchantmentLevel(enchantmentHolder(enchantment), entity);
    }

    public static float getEnchantmentDamageBonus(ItemStack itemStack, LivingEntity attacker, net.minecraft.world.entity.Entity target,
            net.minecraft.world.damagesource.DamageSource damageSource, float baseDamage) {
        if (attacker.level() instanceof ServerLevel serverLevel) {
            return EnchantmentHelper.modifyDamage(serverLevel, itemStack, target, damageSource, baseDamage) - baseDamage;
        }
        return 0;
    }

    public static float getCriticalHitMultiplier(Player player, Entity target, boolean vanillaCritical, float damageModifier) {
        var event = CommonHooks.fireCriticalHit(player, target, vanillaCritical, damageModifier);
        return event.isCriticalHit() ? event.getDamageMultiplier() : 1f;
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

            var breakEvent = CommonHooks.fireBlockBreak(world, gameType, serverPlayer, pos, world.getBlockState(pos));
            if (breakEvent.isCanceled() || breakingPlayer.blockActionRestricted(world, pos, gameType)) {
                return false;
            }

            BlockEntity tileEntity = world.getBlockEntity(pos);
            BlockState destroyedState = blockState.getBlock().playerWillDestroy(world, pos, blockState, breakingPlayer);
            boolean canHarvest = !harvest || destroyedState.canHarvestBlock(world, pos, breakingPlayer);
            boolean canRemove = canHarvest
                    && destroyedState.getBlock().onDestroyedByPlayer(destroyedState, world, pos, breakingPlayer, harvest, world.getFluidState(pos));

            if (canRemove) {
                destroyedState.getBlock().destroy(world, pos, destroyedState);

                if (tryReplant) {
                    breakAndReplant(serverWorld, pos, destroyedState, breakingPlayer, toolStack, harvest);
                } else if (harvest) {
                    destroyedState.getBlock().playerDestroy(world, breakingPlayer, pos, destroyedState, tileEntity, toolStack);
                }
            }
            return canRemove;
        } else {
            return blockState.getBlock().onDestroyedByPlayer(blockState, world, pos, breakingPlayer, harvest,
                    world.getFluidState(pos));
        }
    }

    public static boolean tryReplant(ItemStack itemStack, ItemAbility toolAction) {
        return toolAction == ItemAbilities.HOE_DIG && getEnchantmentLevel(Enchantments.SILK_TOUCH, itemStack) > 0;
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
        if (attacker.level() instanceof ServerLevel serverLevel) {
            var damageSource = attacker instanceof Player player
                    ? attacker.damageSources().playerAttack(player)
                    : attacker.damageSources().mobAttack(attacker);

            EnchantmentHelper.runIterationOnEquipment(target, (enchantment, level, enchantedItem) ->
                    enchantment.value().doPostAttack(serverLevel, level, enchantedItem, net.minecraft.world.item.enchantment.EnchantmentTarget.VICTIM,
                            target, damageSource));

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack equipment = attacker.getItemBySlot(slot);
                EnchantmentHelper.runIterationOnItem(equipment, slot, attacker, (enchantment, level, enchantedItem) ->
                        enchantment.value().doPostAttack(serverLevel, level, enchantedItem,
                                net.minecraft.world.item.enchantment.EnchantmentTarget.ATTACKER, target, damageSource));
            }
        }

        int fireAspectLevel = getEnchantmentLevel(Enchantments.FIRE_ASPECT, itemStack);
        if (fireAspectLevel > 0) {
            target.igniteForSeconds(fireAspectLevel * 4);
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
            result += (float) player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MINING_EFFICIENCY);
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

        result *= (float) player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.BLOCK_BREAK_SPEED);

        if (player.isEyeInFluid(FluidTags.WATER)) {
            result *= (float) player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.SUBMERGED_MINING_SPEED);
        }

        if (!player.onGround()) {
            result /= 5.0F;
        }

        if (blockState != null) {
            result = net.neoforged.neoforge.event.EventHooks.getBreakSpeed(player, blockState, result, pos);
        }

        return result;
    }
}
