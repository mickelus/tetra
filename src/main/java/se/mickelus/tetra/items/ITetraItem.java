package se.mickelus.tetra.items;

import se.mickelus.tetra.network.PacketPipeline;

public interface ITetraItem {
    public void clientPreInit();
    public void init(PacketPipeline packetPipeline);
}
