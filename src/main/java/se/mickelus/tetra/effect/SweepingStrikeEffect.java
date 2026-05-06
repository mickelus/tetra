package se.mickelus.tetra.effect;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbility;
import org.apache.commons.lang3.tuple.Pair;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.mutil.util.RotationHelper;
import se.mickelus.tetra.ServerScheduler;
import se.mickelus.tetra.client.particle.SweepingStrikeParticleOption;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.util.ItemAbilityHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@ParametersAreNonnullByDefault
public class SweepingStrikeEffect {
    private static final Cache<UUID, Integer> strikeCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    /**
     * Gets and increments counter for recurrent strike made by the given entity. Expires after a minute.
     *
     * @param entityId The ID of the responsible entity
     * @return The number of recurrent strikes
     */
    private static int getStrikeCounter(UUID entityId) {
        int counter = 0;
        try {
            counter = strikeCache.get(entityId, () -> 0);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        strikeCache.put(entityId, counter + 1);

        return counter;
    }

    public static void causeTruesweepEffect(Player player, ItemStack itemStack) {
        StrikingEffect.effectActionMap.stream()
                .filter(entry -> EffectHelper.getEffectLevel(itemStack, entry.getLeft()) > 0)
                .map(Pair::getRight)
                .findFirst()
                .ifPresent(tool -> {
                    double lookDistance = Optional.ofNullable(player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE))
                            .map(AttributeInstance::getValue)
                            .orElse(4.5d);
                    BlockPos origin = BlockPos.containing(player.getEyePosition().add(player.getViewVector(0).scale(lookDistance)));
                    causeEffect(player.level(), player, itemStack, origin, tool);
                });
    }

    public static void causeEffect(Level world, Player breakingPlayer, ItemStack toolStack, BlockPos origin, ItemAbility tool) {
        if (world.isClientSide) {
            return;
        }

        boolean alternate = isAlternate(breakingPlayer);
        var targets = breakBlocksAround(world, breakingPlayer, toolStack, origin, tool, alternate);

        int minDelay = targets.stream()
                .mapToInt(Pair::getLeft)
                .min()
                .orElse(0);
        int maxDelay = targets.stream()
                .mapToInt(Pair::getLeft)
                .max()
                .orElse(0);
        int duration = maxDelay - minDelay;
        duration = targets.size() > 10 ? duration : duration * 2;
        duration = Math.max(4, duration);

        double distance = breakingPlayer.getEyePosition().subtract(Vec3.atCenterOf(origin)).length();

        causeVfx(breakingPlayer, alternate, duration, (float) distance - 0.5f);

        if (toolStack.getItem() instanceof ItemModularHandheld item) {
            int amount = Math.max(1, targets.size() / 4);

            if (!targets.isEmpty()) {
                item.applyUsageEffects(breakingPlayer, toolStack, amount);
            }
            item.applyDamage(amount, toolStack, breakingPlayer);
        }
    }

    private static void causeVfx(Player player, boolean isAlternate, int duration, float distance) {
        if (player.level() instanceof ServerLevel) {
            Vec3 viewVec = player.getViewVector(0).scale(distance);
            float ox = -Mth.sin(player.getYRot() * Mth.PI / 180F) * 0.5f;
            float oz = Mth.cos(player.getYRot() * Mth.PI / 180F) * 0.5f;
            ((ServerLevel) player.level()).sendParticles(new SweepingStrikeParticleOption(duration, isAlternate, player.getXRot(), player.getYRot()),
                    player.getX() + viewVec.x + ox, player.getY(0.6) + viewVec.y, player.getZ() + viewVec.z + oz,
                    0, 0, 0, 0, 0);
        }
    }

    /**
     * Breaks several blocks around the given blockpos.
     *
     * @param world          the world in which to break blocks
     * @param breakingPlayer the player which is breaking the blocks
     * @param toolStack      the itemstack used to break the blocks
     * @param originPos      the position which to break blocks around
     * @param tool           the type of tool used to break the center block, the tool required to break nearby blocks has to
     *                       match this
     */
    public static List<Pair<Integer, BlockPos>> breakBlocksAround(Level world, Player breakingPlayer, ItemStack toolStack, BlockPos originPos, ItemAbility tool, boolean alternate) {
        if (world.isClientSide) {
            return Collections.emptyList();
        }

        Vec3 playerPosition = breakingPlayer.getEyePosition();
        int playerDistance = Mth.ceil(playerPosition.distanceTo(Vec3.atCenterOf(originPos)));
        Direction facing = breakingPlayer.getDirection();

        double efficiency = CastOptional.cast(toolStack.getItem(), ItemModularHandheld.class)
                .map(item -> item.getToolEfficiency(toolStack, tool))
                .map(eff -> EffectHelper.getModifiedEfficiency(breakingPlayer, toolStack, eff, null, null))
                .orElse(0f);

        double critMultiplier = CastOptional.cast(toolStack.getItem(), ItemModularHandheld.class)
                .map(item -> CritEffect.rollMultiplier(breakingPlayer.getRandom(), item, toolStack))
                .orElse(1d);

        if (critMultiplier != 1) {
            efficiency *= critMultiplier;
            ((ServerLevel) world).sendParticles(ParticleTypes.ENCHANTED_HIT,
                    originPos.getX() + .5f, originPos.getY() + .5f, originPos.getZ() + .5f, 15, 0.2D, 0.2D, 0.2D, 0.0D);
        }

        int jankLevel = EffectHelper.getEffectLevel(toolStack, ItemEffect.janking);
        int skulkTaintLevel = EffectHelper.getEffectLevel(toolStack, ItemEffect.sculkTaint);
        int reachingLevel = EffectHelper.getEffectLevel(toolStack, ItemEffect.reaching);
        float reachingEfficiency = EffectHelper.getEffectEfficiency(toolStack, ItemEffect.reaching);
        float focusLevel = EffectHelper.getEffectEfficiency(toolStack, ItemEffect.sweepingFocus);
        boolean planarSweep = EffectHelper.getEffectLevel(toolStack, ItemEffect.planarSweep) > 0;

        float focus = focusLevel > 0 ? focusLevel - 1 : 0;
        int vertical = planarSweep ? 0 : 1;

        float pitch = planarSweep
                ? Math.abs(breakingPlayer.getXRot()) > 45
                ? Mth.PI / -2 * Math.signum(breakingPlayer.getXRot())
                : 0
                : breakingPlayer.getXRot() / -180f * Mth.PI;

        boolean tryReplant = EffectHelper.tryReplant(toolStack, tool);

        // generates a box "centered" based on the distance between the hit point and the player
//        List<BlockPos> positions = List.of((strikeCounter / 2) % 2 == 0 ? StrikingEffect.sweep1 : StrikingEffect.sweep2);
        List<BlockPos> positions = BlockPos.betweenClosedStream(-16, -vertical, -playerDistance - 1, 16, vertical, 32)
                .map(BlockPos::new)
                .sorted(Comparator.comparingInt(pos -> getPosWeight(pos, focus, playerDistance)))
                .toList();

//        debugPlacement(world, originPos, positions.stream()
//                .map(p -> RotationHelper.rotatePitch(p, breakingPlayer.getXRot() / -180f * Mth.PI))
//                .map(p -> RotationHelper.rotateDirection(p, facing))
//                .map(originPos::offset)
//                .toList(), positions.size());

        List<Pair<Integer, BlockPos>> targets = new ArrayList<>();
        for (BlockPos pos : positions) {
            BlockPos worldPos = Optional.of(pos)
                    .map(p -> RotationHelper.rotatePitch(p, pitch))
                    .map(p -> RotationHelper.rotateDirection(p, facing))
                    .map(originPos::offset)
                    .get();

            BlockState blockState = world.getBlockState(worldPos);
            float blockHardness = blockState.getDestroySpeed(world, worldPos);

            // make sure that only blocks which require the same tool are broken
            if (ItemAbilityHelper.isEffectiveOn(tool, blockState) && blockHardness >= 0 && (!tryReplant || isFullyGrown(blockState))) {
                // check that the tool level is high enough and break the block
                if (ItemAbilityHelper.playerCanDestroyBlock(breakingPlayer, blockState, worldPos, toolStack, tool)) {
                    var reachingFactor = getReachingFactor(playerPosition, worldPos, reachingLevel, reachingEfficiency);
                    // min 0.5 drain to make blocks like grass still "consume" some efficiency
                    efficiency -= Math.max(0.5, blockHardness / reachingFactor + (Math.abs(pos.getX()) + Math.abs(pos.getZ())) * 0.05);

                    if (efficiency >= 0) {
                        targets.add(Pair.of(alternate ? -pos.getX() : pos.getX(), worldPos));
                    }
                } else {
                    break;
                }
            } else if (!blockState.isAir() && !blockState.liquid()) {
                efficiency -= Math.max(Math.abs(blockHardness), 0.5);
            }

            if (efficiency <= 0) {
                break;
            }
        }

        int minDelay = targets.stream()
                .mapToInt(Pair::getLeft)
                .min()
                .orElse(0);

        targets.forEach(pair -> {
            BlockPos pos = pair.getRight();
            int delay = pair.getLeft() - minDelay;
            delay = targets.size() > 10 ? delay : delay * 2;
            BlockState blockState = world.getBlockState(pos);
            enqueueBlockBreak(world, breakingPlayer, toolStack, pos, blockState, delay, tryReplant, jankLevel, skulkTaintLevel);
        });

        return targets;
    }

    private static double getReachingFactor(Vec3 playerPos, BlockPos blockPos, int reachingLevel, float reachingEfficiency) {
        return reachingLevel > 0
                ? ReachingEffect.getMultiplier(reachingLevel, blockPos.distToCenterSqr(playerPos), reachingEfficiency)
                : 1;
    }

    /**
     * Calculate weight for ordering relative positions, to create interesting breaking patterns based on efficiency/hardness
     *
     * @param pos
     * @param focus
     * @return
     */
    private static int getPosWeight(BlockPos pos, float focus, int backDist) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        double res = Math.pow(Math.abs(x), 2.2) * 1.5 * (1 - focus)
                + Math.pow(Math.abs(y) * 4, 2)
                + Math.pow(z, 2) * (1 + focus);

        if (x < 0) {
            res += -x;
        }

        if (z < 0) {
            res += Math.pow(Math.abs(x), 2.2) * (1 - focus)
                    + Math.pow(Math.abs(y) * 4, 2) * 1.5
                    + Math.pow(z * 12f / backDist, 2) * 2 * (1 + focus);
//            res += (Math.pow(16 - Math.abs(x), 2) * (1 - focus) + Math.pow(5 + z, 2) * (1 + focus)) * 0.2;
//            res += (Math.pow(32 - Math.abs(x) * 2, 2) + Math.pow(5 + z, 2)) * 0.1f;
        }

        return (int) res;
    }

    private static boolean isAlternate(Player player) {
        return getStrikeCounter(player.getUUID()) % 2 == 0;
    }

    private static void enqueueBlockBreak(Level world, Player player, ItemStack itemStack, BlockPos pos, BlockState blockState, int delay, boolean tryReplant, int jankLevel, int skulkTaintLevel) {
        if (delay > 0) {
            ServerScheduler.schedule(delay, () -> breakBlock(world, player, itemStack, pos, blockState, tryReplant, jankLevel, skulkTaintLevel));
        } else {
            breakBlock(world, player, itemStack, pos, blockState, tryReplant, jankLevel, skulkTaintLevel);
        }
    }

    private static void breakBlock(Level world, Player player, ItemStack itemStack, BlockPos pos, BlockState blockState, boolean tryReplant, int jankLevel, int skulkTaintLevel) {
        if (EffectHelper.breakBlock(world, player, itemStack, pos, blockState, true, tryReplant)) {
            EffectHelper.sendEventToPlayer((ServerPlayer) player, LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(blockState));

            if (jankLevel > 0) {
                JankEffect.jankItemsDelayed((ServerLevel) world, pos, jankLevel, EffectHelper.getEffectEfficiency(itemStack, ItemEffect.janking), player);
            }

            if (skulkTaintLevel > 0) {
                SculkTaintEffect.perform((ServerLevel) world, pos, skulkTaintLevel, EffectHelper.getEffectEfficiency(itemStack, ItemEffect.sculkTaint));
            }
        }
    }

    private static boolean isFullyGrown(BlockState blockState) {
        return blockState.getBlock() instanceof CropBlock crop && crop.isMaxAge(blockState);
    }

    private static void debugPlacement(Level world, BlockPos origin, List<BlockPos> positions, int count) {
        for (int i = 0; i < positions.size() && i < count; i++) {
            BlockPos pos = positions.get(i);

            if (i > count * 7f / 8) {
                enqueueDebugPlacement(world, pos, Blocks.WHITE_STAINED_GLASS.defaultBlockState(), i * 5 + 100);
            } else if (i > count * 6f / 8) {
                enqueueDebugPlacement(world, pos, Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState(), i * 5 + 100);
            } else if (i > count * 5f / 8) {
                enqueueDebugPlacement(world, pos, Blocks.BLUE_STAINED_GLASS.defaultBlockState(), i * 5 + 100);
            } else if (i > count * 4f / 8) {
                enqueueDebugPlacement(world, pos, Blocks.CYAN_STAINED_GLASS.defaultBlockState(), i * 5 + 100);
            } else if (i > count * 3f / 8) {
                enqueueDebugPlacement(world, pos, Blocks.LIME_STAINED_GLASS.defaultBlockState(), i * 5 + 100);
            } else if (i > count * 2f / 8) {
                enqueueDebugPlacement(world, pos, Blocks.YELLOW_STAINED_GLASS.defaultBlockState(), i * 5 + 100);
            } else if (i > count * 1f / 8) {
                enqueueDebugPlacement(world, pos, Blocks.ORANGE_STAINED_GLASS.defaultBlockState(), i * 5 + 100);
            } else {
                enqueueDebugPlacement(world, pos, Blocks.RED_STAINED_GLASS.defaultBlockState(), i * 5 + 100);
            }
        }
//        world.setBlock(origin.above(2), Blocks.GLOWSTONE.defaultBlockState(), 3);
    }

    private static void enqueueDebugPlacement(Level world, BlockPos pos, BlockState blockState, int delay) {
        ServerScheduler.schedule(delay, () -> world.setBlock(pos, blockState, 3));
    }
}
