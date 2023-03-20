package se.mickelus.tetra.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.SculkSpreader;
import se.mickelus.tetra.ServerScheduler;

public class SculkTaintEffect {

    public static void perform(ServerLevel level, BlockPos target, int effectLevel, float effectEfficiency) {
        if (level.getRandom().nextDouble() * 100 < effectEfficiency) {
            startSpread(level, target, effectLevel);
        }
    }

    public static void startSpread(ServerLevel level, BlockPos target, int severity) {
        level.playSound(null, target, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.PLAYERS, 2.0F, 0.6F + level.getRandom().nextFloat() * 0.4F);

        SculkSpreader spreader = SculkSpreader.createLevelSpreader();
        spreader.addCursors(target, severity);
        ServerScheduler.schedule(level.getRandom().nextInt(3), () -> tickSpread(spreader, level, target, severity * 10));
    }

    public static void tickSpread(SculkSpreader spreader, LevelAccessor level, BlockPos origin, int bailoutCounter) {
        spreader.updateCursors(level, origin, level.getRandom(), true);

        if (bailoutCounter > 0 && spreader.getCursors().size() > 0) {
            ServerScheduler.schedule(level.getRandom().nextInt(3), () -> tickSpread(spreader, level, origin, bailoutCounter - 1));
        } else {
            System.out.println("Bailed on spreader, bailout: " + bailoutCounter + ", cursors: " + spreader.getCursors().size());
        }
    }
}
