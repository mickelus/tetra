package se.mickelus.tetra.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

public abstract class BlockPosPacket extends AbstractPacket {

    protected BlockPos pos;

    public BlockPosPacket() {}

    public BlockPosPacket(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(pos.getX());
        buffer.writeInt(pos.getY());
        buffer.writeInt(pos.getZ());
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        int x = buffer.readInt();
        int y = buffer.readInt();
        int z = buffer.readInt();
        pos = new BlockPos(x, y, z);
    }
}
