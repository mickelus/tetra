package se.mickelus.tetra.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public abstract class BlockPosPacket extends AbstractPacket {

    protected BlockPos pos;

    public BlockPosPacket() {}

    public BlockPosPacket(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(pos.getX());
        buffer.writeInt(pos.getY());
        buffer.writeInt(pos.getZ());
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        int x = buffer.readInt();
        int y = buffer.readInt();
        int z = buffer.readInt();
        pos = new BlockPos(x, y, z);
    }
}
