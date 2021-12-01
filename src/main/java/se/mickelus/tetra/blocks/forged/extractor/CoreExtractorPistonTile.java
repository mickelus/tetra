package se.mickelus.tetra.blocks.forged.extractor;

import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.network.PacketHandler;
import se.mickelus.tetra.util.TileEntityOptional;

public class CoreExtractorPistonTile extends TileEntity implements ITickableTileEntity {
    @ObjectHolder(TetraMod.MOD_ID + ":" + CoreExtractorPistonBlock.unlocalizedName)
    public static TileEntityType<CoreExtractorPistonTile> type;

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
        if (level instanceof ServerWorld) {
            ((ServerWorld) level).sendParticles(ParticleTypes.LARGE_SMOKE,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 0.1, worldPosition.getZ() + 0.5,
                    5,  0, 0, 0, 0.02f);
        }

        level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundCategory.BLOCKS,
                0.1f, 1);

        level.playSound(null, worldPosition, SoundEvents.METAL_FALL, SoundCategory.BLOCKS,
                0.2f, 0.5f);
    }
}
