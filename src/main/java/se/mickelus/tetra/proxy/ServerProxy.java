package se.mickelus.tetra.proxy;

import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ServerProxy implements IProxy {
    @Override
    public Player getClientPlayer() {
        return null;
    }
}
