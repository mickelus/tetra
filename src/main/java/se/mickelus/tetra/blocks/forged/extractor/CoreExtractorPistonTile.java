package se.mickelus.tetra.blocks.forged.extractor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.util.ITetraTicker;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class CoreExtractorPistonTile extends BlockEntity implements ITetraTicker {
    @ObjectHolder(TetraMod.MOD_ID + ":" + CoreExtractorPistonBlock.unlocalizedName)
    public static BlockEntityType<CoreExtractorPistonTile> type;

    static final long activationDuration = 105;
    private static final int fillAmount = 40;

    private long endTime = Long.MAX_VALUE;

    public CoreExtractorPistonTile(BlockPos p_155268_, BlockState p_155269_) {
        super(type, p_155268_, p_155269_);
    }


    public void activate() {
        if (!isActive()) {
            endTime = level.getGameTime() + activationDuration;

            if (!level.isClientSide) {
                TetraMod.packetHandler.sendToAllPlayersNear(new CoreExtractorPistonUpdatePacket(worldPosition, endTime), worldPosition, 64, level.dimension());
            }
        }
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public boolean isActive() {
        return endTime != Long.MAX_VALUE;
    }

    public float getProgress(float partialTicks) {
        if (isActive()) {
            return Math.min(1, Math.max(0, (level.getGameTime() + activationDuration - endTime + partialTicks) / activationDuration));
        }
        return 0;
    }

    private void runEndEffects() {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 0.1, worldPosition.getZ() + 0.5,
                    5,  0, 0, 0, 0.02f);
        }

        level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                0.1f, 1);

        level.playSound(null, worldPosition, SoundEvents.METAL_FALL, SoundSource.BLOCKS,
                0.2f, 0.5f);
    }

	@Override
	public void tick(Level level, BlockPos pos, BlockState state) {
		if (endTime < level.getGameTime()) {
			endTime = Long.MAX_VALUE;
			if (!level.isClientSide) {
				TileEntityOptional.from(level, pos.relative(Direction.DOWN), CoreExtractorBaseTile.class)
					.ifPresent(base -> base.fill(fillAmount));

				runEndEffects();
				setChanged();
			}
		}
	}
}
