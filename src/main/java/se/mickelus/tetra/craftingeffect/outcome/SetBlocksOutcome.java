package se.mickelus.tetra.craftingeffect.outcome;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.ServerScheduler;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;
import se.mickelus.tetra.util.StreamHelper;

import java.util.Arrays;
import java.util.Map;

public class SetBlocksOutcome implements CraftingEffectOutcome {
    BlockState blockState;
    PropertyMatcher requirement = PropertyMatcher.any;
    boolean requireReplaceable = true;
    boolean solidBelow = false;
    boolean solidAdjacent = false;
    boolean skipCenter = false;
    int radius = 5;
    int count = 1;
    float chance = 1;

    SoundOptions sound;
    ParticleOptions particle;

    int delay = 0;
    int randomDelay = 0;
    int fxInterval = 10;
    ParticleOptions intervalParticle;
    SoundOptions intervalSound;
    float intervalSoundVolumeChange = 0;
    float intervalSoundPitchChange = 0;


    @Override
    public boolean apply(ResourceLocation[] unlockedEffects, ItemStack upgradedStack, String slot, boolean isReplacing, Player player,
            ItemStack[] preMaterials, Map<ToolAction, Integer> tools, Level world, UpgradeSchematic schematic, BlockPos pos, BlockState blockState,
            boolean consumeResources, ItemStack[] postMaterials, float severity) {
        if (consumeResources && !world.isClientSide() && world.getRandom().nextFloat() < chance) {
            setBlocksAround((ServerLevel) world, pos);
        }
        return true;
    }

    private void setBlocksAround(ServerLevel level, BlockPos origin) {
        BlockPos.withinManhattanStream(origin, radius, radius, radius)
                .map(BlockPos::new)
                .filter(blockPos -> !origin.equals(blockPos) || !skipCenter)
                .collect(StreamHelper.toShuffledList())
                .stream()
                .filter(blockPos -> blockState.canSurvive(level, blockPos))
                .filter(blockPos -> testRequirements(level, level.getBlockState(blockPos), blockPos))
                .limit(count)
                .forEach(blockPos -> setBlock(level, blockPos));
    }

    private void setBlock(ServerLevel level, BlockPos pos) {
        if (delay > 0 || randomDelay > 0) {
            int actualDelay = delay + (randomDelay > 0 ? level.getRandom().nextInt(randomDelay) : 0);
            ServerScheduler.schedule(actualDelay, () -> {
                if (blockState.canSurvive(level, pos) && testRequirements(level, level.getBlockState(pos), pos)) {
                    level.setBlock(pos, blockState, Block.UPDATE_ALL);
                    if (particle != null) {
                        level.sendParticles(particle, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                8, 0.5, 0.5, 0.5, 0.1);
                    }
                    if (sound != null) {
                        level.playSound(null, pos, sound.type, SoundSource.BLOCKS, sound.volume, sound.pitch);
                    }
                }
            });
            if (intervalParticle != null && fxInterval > 0) {
                for (int i = 1; i * fxInterval < actualDelay; i++) {
                    ServerScheduler.schedule(i * fxInterval, () -> level.sendParticles(intervalParticle, pos.getX() + 0.5, pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            8, 0.5, 0.5, 0.5, 0.1));
                }
            }
            if (intervalSound != null && fxInterval > 0) {
                for (int i = 1; i * fxInterval < actualDelay; i++) {
                    float volume = intervalSound.volume + i * intervalSoundVolumeChange;
                    float pitch = intervalSound.pitch + i * intervalSoundPitchChange;
                    ServerScheduler.schedule(i * fxInterval, () -> level.playSound(null, pos, intervalSound.type, SoundSource.PLAYERS, volume,
                            pitch));
                }
            }
        } else {
            level.setBlock(pos, blockState, Block.UPDATE_ALL);
            if (particle != null) {
                level.sendParticles(particle, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        8, 0.5, 0.5, 0.5, 0.1);
            }
            if (sound != null) {
                level.playSound(null, pos, sound.type, SoundSource.BLOCKS, sound.volume, sound.pitch);
            }
        }
    }

    private boolean testRequirements(Level level, BlockState blockState, BlockPos blockPos) {
        return requirement.test(blockState)
                && (!requireReplaceable || blockState.canBeReplaced())
                && (!solidBelow || MultifaceBlock.canAttachTo(level, Direction.DOWN, blockPos.below(), level.getBlockState(blockPos.below())))
                && (!solidAdjacent || isSolidAdjacent(level, blockPos));
    }

    private boolean isSolidAdjacent(Level level, BlockPos blockPos) {
        return Arrays.stream(Direction.values())
                .anyMatch(dir -> MultifaceBlock.canAttachTo(level, dir, blockPos.relative(dir), level.getBlockState(blockPos.relative(dir))));
    }

    static class SoundOptions {
        SoundEvent type;
        float volume = 1f;
        float pitch = 1f;
    }
}
