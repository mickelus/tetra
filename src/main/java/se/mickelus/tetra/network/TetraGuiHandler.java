package se.mickelus.tetra.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public interface TetraGuiHandler {
    public static final int toolbeltId = 0;
    public static final int workbenchId = 1;
    public static final int forgedContainerId = 2;

    public Object getServerGuiElement(EntityPlayer player, World world, int x, int y, int z);
    public Object getClientGuiElement(EntityPlayer player, World world, int x, int y, int z);
}
