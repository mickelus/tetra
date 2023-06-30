package se.mickelus.tetra.blocks.holo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import se.mickelus.tetra.interactions.PerformSide;
import se.mickelus.tetra.interactions.SecondaryInteractionBase;

public class ToggleScanModeInteraction extends SecondaryInteractionBase {
    private boolean enable;

    public ToggleScanModeInteraction(String key, boolean enable) {
        super(key, PerformSide.both);
        this.enable = enable;
    }

    @Override
    public boolean canPerform(Player player, Level level, @Nullable BlockPos pos, @Nullable Entity target) {
        if (pos != null && level.getBlockState(pos).is(HolosphereBlock.instance.get())) {
            return level.getBlockEntity(pos, HolosphereBlockEntity.type.get())
                    .map(entity -> entity.canScan() && entity.inScanMode() != this.enable)
                    .orElse(false);
        }
        return false;
    }

    @Override
    public void perform(Player player, Level level, @Nullable BlockPos pos, @Nullable Entity target) {
        level.getBlockEntity(pos, HolosphereBlockEntity.type.get()).ifPresent(entity -> entity.toggleScanMode(enable));
    }
}
