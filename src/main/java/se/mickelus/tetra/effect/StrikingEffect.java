package se.mickelus.tetra.effect;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.ServerScheduler;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.util.CastOptional;
import se.mickelus.tetra.util.RotationHelper;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class StrikingEffect {
    private static final Cache<UUID, Integer> strikeCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();


    private static final BlockPos[] sweep1 = new BlockPos[] {
            new BlockPos(-2, 0, 0),
            new BlockPos(-1, 0, 0),
            new BlockPos(0, 0, 0),
            new BlockPos(1, 0, 0),
            new BlockPos(2, 0, 0),
            new BlockPos(-1, 0, 1),
            new BlockPos(0, 0, 1),
            new BlockPos(1, 0, 1),
            new BlockPos(-3, 0, -1),
            new BlockPos(-2, 0, -1),
            new BlockPos(-1, 0, -1),
            new BlockPos(0, 0, -1),
            new BlockPos(3, 0, -1),
            new BlockPos(-3, 0, -2),
            new BlockPos(-2, 0, -2),
            new BlockPos(-1, 0, -2),
            new BlockPos(-1, 1, 0),
            new BlockPos(0, 1, 0),
            new BlockPos(-2, 1, -1),
            new BlockPos(-1, 1, -1),
            new BlockPos(-1, -1, 0),
            new BlockPos(0, -1, 0),
            new BlockPos(-2, -1, -1),
            new BlockPos(-1, -1, -1),
    };

    private static final BlockPos[] sweep2 = new BlockPos[] {
            new BlockPos(-2, 0, 0),
            new BlockPos(-1, 0, 0),
            new BlockPos(0, 0, 0),
            new BlockPos(1, 0, 0),
            new BlockPos(2, 0, 0),
            new BlockPos(3, 0, 0),
            new BlockPos(-1, 0, 1),
            new BlockPos(0, 0, 1),
            new BlockPos(1, 0, 1),
            new BlockPos(2, 0, 1),
            new BlockPos(-2, 0, -1),
            new BlockPos(-1, 0, -1),
            new BlockPos(0, 0, -1),
            new BlockPos(4, 0, -1),
            new BlockPos(-1, 1, 0),
            new BlockPos(0, 1, 0),
            new BlockPos(1, 1, 0),
            new BlockPos(-1, -1, 0),
            new BlockPos(0, -1, 0),
            new BlockPos(1, -1, 0),
    };

    public static boolean causeEffect(PlayerEntity breakingPlayer, ItemStack itemStack, ItemModularHandheld item, World world, BlockPos pos, BlockState blockState) {
        int strikingLevel = 0;
        ToolType tool = null;

        // essentially checks if the item is effective in for each tool type, and checks if it can strike for that type
        if (ItemModularHandheld.isToolEffective(ToolType.AXE, blockState)) {
            strikingLevel = EffectHelper.getEffectLevel(itemStack, ItemEffect.strikingAxe);
            if (strikingLevel > 0) {
                tool = ToolType.AXE;
            }
        }

        if (strikingLevel <= 0 && ItemModularHandheld.isToolEffective(ToolType.PICKAXE, blockState)) {
            strikingLevel = EffectHelper.getEffectLevel(itemStack, ItemEffect.strikingPickaxe);
            if (strikingLevel > 0) {
                tool = ToolType.PICKAXE;
            }
        }

        if (strikingLevel <= 0 && ItemModularHandheld.isToolEffective(ToolTypes.cut, blockState)) {
            strikingLevel = EffectHelper.getEffectLevel(itemStack, ItemEffect.strikingCut);
            if (strikingLevel > 0) {
                tool = ToolTypes.cut;
            }
        }

        if (strikingLevel <= 0 && ItemModularHandheld.isToolEffective(ToolType.SHOVEL, blockState)) {
            strikingLevel = EffectHelper.getEffectLevel(itemStack, ItemEffect.strikingShovel);
            if (strikingLevel > 0) {
                tool = ToolType.SHOVEL;
            }
        }

        if (strikingLevel <= 0 && ItemModularHandheld.isToolEffective(ToolType.HOE, blockState)) {
            strikingLevel = EffectHelper.getEffectLevel(itemStack, ItemEffect.strikingHoe);
            if (strikingLevel > 0) {
                tool = ToolType.HOE;
            }
        }

        if (strikingLevel > 0) {
            int sweepingLevel = EffectHelper.getEffectLevel(itemStack, ItemEffect.sweepingStrike);
            if (breakingPlayer.getAttackStrengthScale(0) > 0.9 && blockState.getDestroySpeed(world, pos) != -1) {
                if (sweepingLevel > 0) {
                    breakBlocksAround(world, breakingPlayer, itemStack, pos, tool, sweepingLevel);
                } else {
                    int toolLevel = itemStack.getItem().getHarvestLevel(itemStack, tool, breakingPlayer, blockState);
                    if ((toolLevel >= 0 && toolLevel >= blockState.getBlock().getHarvestLevel(blockState))
                            || itemStack.isCorrectToolForDrops(blockState)) {
                        EffectHelper.breakBlock(world, breakingPlayer, itemStack, pos, blockState, true);
                    }
                }

                item.applyUsageEffects(breakingPlayer, itemStack, 1);
                item.applyDamage(item.getBlockDestroyDamage(), itemStack, breakingPlayer);
            }
            breakingPlayer.resetAttackStrengthTicker();
            return true;
        }

        return false;
    }

    /**
     * Gets and increments counter for recurrent strike made by the given entity. Expires after a minute.
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

    /**
     * Breaks several blocks around the given blockpos.
     * @param world the world in which to break blocks
     * @param breakingPlayer the player which is breaking the blocks
     * @param toolStack the itemstack used to break the blocks
     * @param originPos the position which to break blocks around
     * @param tool the type of tool used to break the center block, the tool required to break nearby blocks has to
     *             match this
     * @param sweepingLevel the level of the sweeping effect on the toolStack
     */
    private static void breakBlocksAround(World world, PlayerEntity breakingPlayer, ItemStack toolStack, BlockPos originPos, ToolType tool,
            int sweepingLevel) {
        if (world.isClientSide) {
            return;
        }

        Direction facing = breakingPlayer.getDirection();
        final int strikeCounter = getStrikeCounter(breakingPlayer.getUUID());
        final boolean alternate = strikeCounter % 2 == 0;

        double efficiency = CastOptional.cast(toolStack.getItem(), ItemModularHandheld.class)
                .map(item -> item.getToolEfficiency(toolStack, tool))
                .orElse(0f);

        double critMultiplier = CastOptional.cast(toolStack.getItem(), ItemModularHandheld.class)
                .map(item -> CritEffect.rollMultiplier(breakingPlayer.getRandom(), item, toolStack))
                .orElse(1d);

        if (critMultiplier != 1) {
            efficiency *= critMultiplier;
            ((ServerWorld) world).sendParticles(ParticleTypes.ENCHANTED_HIT,
                    originPos.getX() + .5f, originPos.getY() + .5f, originPos.getZ() + .5f, 15, 0.2D, 0.2D, 0.2D, 0.0D);
        }

        breakingPlayer.sweepAttack();

        int[] delays = Arrays.stream((strikeCounter / 2) % 2 == 0 ? sweep1 : sweep2)
                .map(BlockPos::getX)
                .mapToInt(x -> x + 3)
                .toArray();

        List<BlockPos> positions = Arrays.stream((strikeCounter / 2) % 2 == 0 ? sweep1 : sweep2)
                .map(pos -> (alternate ? new BlockPos(-pos.getX(), pos.getY(), pos.getZ()) : pos))
                .map(pos -> RotationHelper.rotatePitch(pos, breakingPlayer.xRot))
                .map(pos -> RotationHelper.rotateDirection(pos, facing))
                .map(originPos::offset)
                .collect(Collectors.toList());

        for (int i = 0; i < positions.size(); i++) {
            BlockPos pos = positions.get(i);
            BlockState blockState = world.getBlockState(pos);
            float blockHardness = blockState.getDestroySpeed(world, pos);

            // make sure that only blocks which require the same tool are broken
            if (ItemModularHandheld.isToolEffective(tool, blockState) && blockHardness != -1) {

                // check that the tool level is high enough and break the block
                int toolLevel = toolStack.getItem().getHarvestLevel(toolStack, tool, breakingPlayer, blockState);
                if ((toolLevel >= 0 && toolLevel >= blockState.getBlock().getHarvestLevel(blockState))
                        || toolStack.isCorrectToolForDrops(blockState)) {

                    // adds a fixed amount to make blocks like grass still "consume" some efficiency
                    efficiency -= blockHardness + 0.5;

                    enqueueBlockBreak(world, breakingPlayer, toolStack, pos, blockState, tool, toolLevel, delays[i]);
                } else {
                    break;
                }
            } else if (blockState.canOcclude()) {
                efficiency -= Math.abs(blockHardness);
            }

            if (efficiency <= 0) {
                break;
            }
        }
    }

    private static void enqueueBlockBreak(World world, PlayerEntity player, ItemStack itemStack, BlockPos pos, BlockState blockState, ToolType tool,
            int toolLevel, int delay) {
        ServerScheduler.schedule(delay, () -> {
            if (((toolLevel >= 0 && toolLevel >= blockState.getBlock().getHarvestLevel(blockState))
                    || itemStack.isCorrectToolForDrops(blockState))
                    && ItemModularHandheld.isToolEffective(tool, blockState)) {
                if (EffectHelper.breakBlock(world, player, itemStack, pos, blockState, true)) {
                    EffectHelper.sendEventToPlayer((ServerPlayerEntity) player, 2001, pos, Block.getId(blockState));
                }
            }
        });
    }

}
