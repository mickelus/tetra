package se.mickelus.tetra.items;

import se.mickelus.tetra.network.PacketHandler;

public interface ITetraItem {
    public void clientPreInit();
    public void init(PacketHandler packetHandler);
}
