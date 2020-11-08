package se.mickelus.tetra.effect;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPlaySoundEventPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import se.mickelus.tetra.items.modular.ModularItem;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class EffectHelper {
    private static final Cache<UUID, Float> cooledAttackStrengthCache = CacheBuilder.newBuilder()
            .maximumSize(50)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build();

    public static void setCooledAttackStrength(PlayerEntity player, float strength) {
        cooledAttackStrengthCache.put(player.getUniqueID(), strength);
    }

    public static float getCooledAttackStrength(PlayerEntity player) {
        try {
            return cooledAttackStrengthCache.get(player.getUniqueID(), () -> 0f);
        } catch (ExecutionException e) {
            return 0;
        }
    }


    public static int getEffectLevel(ItemStack itemStack, ItemEffect effect) {
        ModularItem item = (ModularItem) itemStack.getItem();
        return item.getEffectLevel(itemStack, effect);
    }

    public static double getEffectEfficiency(ItemStack itemStack, ItemEffect effect) {
        ModularItem item = (ModularItem) itemStack.getItem();
        return item.getEffectEfficiency(itemStack, effect);
    }

    /**
     * Break a block in the world, as a player. Based on how players break blocks in vanilla.
     * @param world the world in which to break blocks
     * @param breakingPlayer the player which is breaking the blocks
     * @param toolStack the itemstack used to break the blocks
     * @param pos the position which to break blocks around
     * @param blockState the state of the block that is to broken
     * @param harvest true if the player is ment to harvest the block, false if it should just magically disappear
     * @return True if the player was allowed to break the block, otherwise false
     */
    public static boolean breakBlock(World world, PlayerEntity breakingPlayer, ItemStack toolStack, BlockPos pos, BlockState blockState,
            boolean harvest) {
        if (!world.isRemote) {
            ServerWorld serverWorld = (ServerWorld) world;
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) breakingPlayer;
            GameType gameType = serverPlayer.interactionManager.getGameType();

            int exp = net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(world, gameType, serverPlayer, pos);

            if (exp != -1) {
                boolean canRemove = !toolStack.onBlockStartBreak(pos, breakingPlayer)
                        && !breakingPlayer.blockActionRestricted(world, pos, gameType)
                        && (!harvest || blockState.canHarvestBlock(world, pos, breakingPlayer))
                        && blockState.getBlock().removedByPlayer(blockState, world, pos, breakingPlayer, harvest, world.getFluidState(pos));

                if (canRemove) {
                    blockState.getBlock().onPlayerDestroy(world, pos, blockState);

                    if (harvest) {
                        blockState.getBlock().harvestBlock(world, breakingPlayer, pos, blockState, world.getTileEntity(pos), toolStack);

                        if (exp > 0) {
                            blockState.getBlock().dropXpOnBlockBreak(serverWorld, pos, exp);
                        }
                    }
                }
                return canRemove;
            }

            return false;
        } else {
            return blockState.getBlock().removedByPlayer(blockState, world, pos, breakingPlayer, harvest,
                    world.getFluidState(pos));
        }
    }

    /**
     * Sends an event to a specific player.
     * @param player the player the event will be sent to
     * @param type an integer representation of the event
     * @param pos the position in which the event takes place
     * @param data an integer representation of event data (e.g. Block.getStateId)
     */
    public static void sendEventToPlayer(ServerPlayerEntity player, int type, BlockPos pos, int data) {
        player.connection.sendPacket(new SPlaySoundEventPacket(type, pos, data, false));
    }

    /**
     * Variant on {@link EnchantmentHelper#applyArthropodEnchantments} that allows control over the held itemstack
     * @param itemStack
     * @param target
     * @param attacker
     */
    public static void applyEnchantmentHitEffects(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
        EnchantmentHelper.getEnchantments(itemStack).forEach((enchantment, level) -> {
            enchantment.onEntityDamaged(attacker, target, level);
        });

        if (attacker != null) {
            for (ItemStack equipment: attacker.getEquipmentAndArmor()) {
                EnchantmentHelper.getEnchantments(equipment).forEach((enchantment, level) -> {
                    enchantment.onEntityDamaged(attacker, target, level);
                });
            }
        }
    }
}
