package se.mickelus.tetra.blocks;

import se.mickelus.tetra.network.PacketPipeline;

public interface ITetraBlock {
    public void clientPreInit();
    public void init(PacketPipeline packetPipeline);
}
