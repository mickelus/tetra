package se.mickelus.tetra.effect;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import se.mickelus.tetra.items.modular.ModularItem;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CritEffect {
    private static final Cache<UUID, Boolean> critBlockCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build();

    public static boolean critBlock(World world, PlayerEntity breakingPlayer, BlockPos pos, BlockState blockState, ItemStack itemStack, int critLevel) {
        if (breakingPlayer.getRNG().nextFloat() < critLevel * 0.01
                && critBlockCache.getIfPresent(breakingPlayer.getUniqueID()) == null
                && blockState.getBlockHardness(world, pos) > -1
                && itemStack.getItem().getDestroySpeed(itemStack, blockState) > 2 * blockState.getBlockHardness(world, pos)) {

            ToolType tool = blockState.getHarvestTool();
            int toolLevel = itemStack.getItem().getHarvestLevel(itemStack, tool, breakingPlayer, blockState);

            if (( toolLevel >= 0 && toolLevel >= blockState.getBlock().getHarvestLevel(blockState) ) || itemStack.canHarvestBlock(blockState)) {
                EffectHelper.breakBlock(world, breakingPlayer, itemStack, pos, blockState, true);
                itemStack.getItem().onBlockDestroyed(itemStack, world, blockState, pos, breakingPlayer);

                critBlockCache.put(breakingPlayer.getUniqueID(), true);

                if (breakingPlayer instanceof ServerPlayerEntity) {
                    EffectHelper.sendEventToPlayer((ServerPlayerEntity) breakingPlayer, 2001, pos, Block.getStateId(blockState));
                }

                if (world instanceof ServerWorld) {
                    ((ServerWorld) world).spawnParticle(ParticleTypes.ENCHANTED_HIT,
                            pos.getX() + 0.5, // world.rand.nextGaussian(),
                            pos.getY() + 0.5, // world.rand.nextGaussian(),
                            pos.getZ() + 0.5, // world.rand.nextGaussian(),
                            12,
                            (world.rand.nextDouble() * 2.0D - 1.0D) * 0.3D,
                            0.3D + world.rand.nextDouble() * 0.3D,
                            (world.rand.nextDouble() * 2.0D - 1.0D) * 0.3D,
                            0.3);
                }

                return true;
            }
        }

        return false;
    }

    public static void onBlockBreak(LivingEntity entity) {
        critBlockCache.invalidate(entity.getUniqueID());
    }

    public static void critEntity(CriticalHitEvent event, ItemStack itemStack, int critLevel) {
        if (event.getEntityLiving().getRNG().nextFloat() < critLevel * 0.01) {
            event.setDamageModifier(Math.max((float) EffectHelper.getEffectEfficiency(itemStack, ItemEffect.criticalStrike), event.getDamageModifier()));
            event.setResult(Event.Result.ALLOW);
        }
    }

    /**
     * Attempts to roll for a crit multiplier with the given item & entity
     * @param random a random instance, preferably taken from the causing entity
     * @param item
     * @param itemStack
     * @return returns 1 if the item cannot crit or if the didn't result in a crit, otherwise the crit multiplier (efficiency) for the item
     */
    public static double rollMultiplier(Random random, ModularItem item, ItemStack itemStack) {
        int critLevel = item.getEffectLevel(itemStack, ItemEffect.criticalStrike);
        if (critLevel > 0 && random.nextFloat() < critLevel * 0.01) {
            return item.getEffectEfficiency(itemStack, ItemEffect.criticalStrike);
        }

        return 1;
    }
}
