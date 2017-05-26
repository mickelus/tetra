package se.mickelus.tetra.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public interface TetraGuiHandler {
    public Object getServerGuiElement(EntityPlayer player, World world, int x, int y, int z);
    public Object getClientGuiElement(EntityPlayer player, World world, int x, int y, int z);
}
