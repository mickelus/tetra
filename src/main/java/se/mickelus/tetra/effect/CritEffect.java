package se.mickelus.tetra.effect;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
@ParametersAreNonnullByDefault
public class CritEffect {
    private static final Cache<UUID, BlockPos> critBlockCache = CacheBuilder.newBuilder()
            .maximumSize(50)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build();

    public static boolean critBlock(Level world, Player breakingPlayer, BlockPos pos, BlockState blockState, ItemStack itemStack, int critLevel) {
        BlockPos recentCritPos = critBlockCache.getIfPresent(breakingPlayer.getUUID());

        // this avoids some log spam from when the client attempts to abort breaking of a critted block
        if (pos.equals(recentCritPos)) {
            return true;
        }

        if (breakingPlayer.getRandom().nextFloat() < critLevel * 0.01
                && recentCritPos == null
                && blockState.getDestroySpeed(world, pos) > -1
                && itemStack.getItem().getDestroySpeed(itemStack, blockState) > 2 * blockState.getDestroySpeed(world, pos)) {

            ToolType tool = blockState.getHarvestTool();
            int toolLevel = itemStack.getItem().getHarvestLevel(itemStack, tool, breakingPlayer, blockState);

            if (( toolLevel >= 0 && toolLevel >= blockState.getBlock().getHarvestLevel(blockState) ) || itemStack.isCorrectToolForDrops(blockState)) {
                EffectHelper.breakBlock(world, breakingPlayer, itemStack, pos, blockState, true);
                itemStack.getItem().mineBlock(itemStack, world, blockState, pos, breakingPlayer);

                critBlockCache.put(breakingPlayer.getUUID(), pos);

                CastOptional.cast(breakingPlayer, ServerPlayer.class)
                        .ifPresent(serverPlayer -> EffectHelper.sendEventToPlayer(serverPlayer, 2001, pos, Block.getId(blockState)));

                if (world instanceof ServerLevel) {
                    ((ServerLevel) world).sendParticles(ParticleTypes.ENCHANTED_HIT,
                            pos.getX() + 0.5, // world.rand.nextGaussian(),
                            pos.getY() + 0.5, // world.rand.nextGaussian(),
                            pos.getZ() + 0.5, // world.rand.nextGaussian(),
                            12,
                            (world.random.nextDouble() * 2.0D - 1.0D) * 0.3D,
                            0.3D + world.random.nextDouble() * 0.3D,
                            (world.random.nextDouble() * 2.0D - 1.0D) * 0.3D,
                            0.3);
                }

                return true;
            }
        }

        return false;
    }

    public static void onBlockBreak(LivingEntity entity) {
        critBlockCache.invalidate(entity.getUUID());
    }

    public static void critEntity(CriticalHitEvent event, ItemStack itemStack, int critLevel) {
        if (event.getEntityLiving().getRandom().nextFloat() < critLevel * 0.01) {
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
    public static double rollMultiplier(Random random, IModularItem item, ItemStack itemStack) {
        int critLevel = item.getEffectLevel(itemStack, ItemEffect.criticalStrike);
        if (critLevel > 0 && random.nextFloat() < critLevel * 0.01) {
            return item.getEffectEfficiency(itemStack, ItemEffect.criticalStrike);
        }

        return 1;
    }
}
