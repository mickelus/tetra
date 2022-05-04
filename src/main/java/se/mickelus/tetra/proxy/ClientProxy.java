package se.mickelus.tetra.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ClientProxy implements IProxy {
    @Override
    public Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }
}
