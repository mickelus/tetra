package se.mickelus.tetra.blocks.workbench;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import se.mickelus.tetra.network.AbstractPacket;

public class UpdateWorkbenchPacket extends AbstractPacket {

    private BlockPos pos;
    private int state;

    public UpdateWorkbenchPacket() {}

    public UpdateWorkbenchPacket(BlockPos pos, int state) {
        this.pos = pos;
        this.state = state;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        buffer.writeInt(pos.getX());
        buffer.writeInt(pos.getY());
        buffer.writeInt(pos.getZ());

        buffer.writeInt(state);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        int x = buffer.readInt();
        int y = buffer.readInt();
        int z = buffer.readInt();
        pos = new BlockPos(x, y, z);

        state = buffer.readInt();
    }

    @Override
    public void handleClientSide(EntityPlayer player) {

    }

    @Override
    public void handleServerSide(EntityPlayer player) {
        TileEntityWorkbench workbench = (TileEntityWorkbench) player.worldObj.getTileEntity(pos);
        if (workbench != null) {
            workbench.setCurrentState(state);
        }
    }
}
