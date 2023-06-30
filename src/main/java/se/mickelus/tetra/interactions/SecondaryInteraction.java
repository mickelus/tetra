package se.mickelus.tetra.interactions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public interface SecondaryInteraction {
    public String getKey();

    public String getLabel();

    public boolean canPerform(Player player, Level level, @Nullable BlockPos pos, @Nullable Entity target);

    public void perform(Player player, Level level, @Nullable BlockPos pos, @Nullable Entity target);

    public PerformSide getPerformSide();
}
