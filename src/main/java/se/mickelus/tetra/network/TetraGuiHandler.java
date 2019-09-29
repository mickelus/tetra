package se.mickelus.tetra.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public interface TetraGuiHandler {
    public static final int toolbeltId = 0;
    public static final int workbenchId = 1;
    public static final int forgedContainerId = 2;

    public Object getServerGuiElement(PlayerEntity player, World world, int x, int y, int z);
    public Object getClientGuiElement(PlayerEntity player, World world, int x, int y, int z);
}
