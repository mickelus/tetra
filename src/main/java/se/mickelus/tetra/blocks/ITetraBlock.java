package se.mickelus.tetra.blocks;

import se.mickelus.tetra.network.PacketHandler;

public interface ITetraBlock {
    public void clientPreInit();
    public void init(PacketHandler packetHandler);
}
