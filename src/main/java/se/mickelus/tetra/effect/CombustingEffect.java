package se.mickelus.tetra.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.util.StreamHelper;

import java.util.concurrent.atomic.AtomicBoolean;

public class CombustingEffect {
    public static void perform(LivingEntity entity, ItemStack itemStack, double multiplier) {
        if (!entity.level().isClientSide) {
            int effectLevel = (int) Math.round(EffectHelper.getEffectLevel(itemStack, ItemEffect.combusting) * multiplier);
            if (effectLevel > 0 && entity.getRandom().nextFloat() < EffectHelper.getEffectEfficiency(itemStack, ItemEffect.combusting) / 100) {
                if (igniteBlocksAround(entity.level(), entity.blockPosition(), 4, effectLevel, true, false)) {
                    Vec3 pos = entity.blockPosition().getCenter();
                    ((ServerLevel) entity.level()).sendParticles(ParticleTypes.LAVA, pos.x, pos.y, pos.z, 2, 0, 0, 0, 0.06f);
                    ((ServerLevel) entity.level()).sendParticles(ParticleTypes.LARGE_SMOKE, pos.x, pos.y, pos.z, 2, 0, 0, 0, 0);
                    entity.level().playSound(null, entity.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.6f, 1.2f);
                }
            }
        }
    }

    public static boolean igniteBlocksAround(Level level, BlockPos origin, int radius, int count, boolean skipCenter, boolean soulfire) {
        AtomicBoolean success = new AtomicBoolean(false);
        Block fireBlock = soulfire ? Blocks.SOUL_FIRE : Blocks.FIRE;
        BlockPos.withinManhattanStream(origin, radius, radius, radius)
                .map(BlockPos::new)
                .filter(blockPos -> !origin.equals(blockPos) || !skipCenter)
                .collect(StreamHelper.toShuffledList())
                .stream()
                .filter(level::isEmptyBlock)
                .filter(blockPos -> soulfire && canSoulfireSpawn(level, blockPos) || !soulfire && fireBlock.canSurvive(fireBlock.defaultBlockState(), level, blockPos))
                .limit(count)
                .forEach(blockPos -> {
                    if (level.setBlock(blockPos, fireBlock.defaultBlockState(), 2)) {
                        success.set(true);
                    }
                });

        return success.get();
    }

    private static boolean canSoulfireSpawn(Level level, BlockPos pos) {
        BlockPos belowPos = pos.immutable().below();
        return Block.isShapeFullBlock(level.getBlockState(belowPos).getCollisionShape(level, belowPos));
    }
}
