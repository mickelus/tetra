package se.mickelus.tetra.blocks.forged.extractor;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.util.TileEntityOptional;

public class CoreExtractorPistonTile extends BlockEntity implements TickableBlockEntity {
    @ObjectHolder(TetraMod.MOD_ID + ":" + CoreExtractorPistonBlock.unlocalizedName)
    public static BlockEntityType<CoreExtractorPistonTile> type;

    static final long activationDuration = 105;
    private static final int fillAmount = 40;

    private long endTime = Long.MAX_VALUE;

    public CoreExtractorPistonTile() {
        super(type);
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

    @Override
    public void tick() {
        if (endTime < level.getGameTime()) {
            endTime = Long.MAX_VALUE;
            if (!level.isClientSide) {
                TileEntityOptional.from(level, worldPosition.relative(Direction.DOWN), CoreExtractorBaseTile.class)
                        .ifPresent(base -> base.fill(fillAmount));

                runEndEffects();
                setChanged();
            }
        }
    }

    private void runEndEffects() {
        if (level instanceof ServerLevel) {
            ((ServerLevel) level).sendParticles(ParticleTypes.LARGE_SMOKE,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 0.1, worldPosition.getZ() + 0.5,
                    5,  0, 0, 0, 0.02f);
        }

        level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                0.1f, 1);

        level.playSound(null, worldPosition, SoundEvents.METAL_FALL, SoundSource.BLOCKS,
                0.2f, 0.5f);
    }
}
