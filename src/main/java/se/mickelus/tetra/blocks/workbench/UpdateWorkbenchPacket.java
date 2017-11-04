package se.mickelus.tetra.blocks.workbench;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.UpgradeSchema;
import se.mickelus.tetra.network.AbstractPacket;

import java.io.IOException;

public class UpdateWorkbenchPacket extends AbstractPacket {

    private BlockPos pos;
    private UpgradeSchema schema;

    public UpdateWorkbenchPacket() {}

    public UpdateWorkbenchPacket(BlockPos pos, UpgradeSchema schema) {
        this.pos = pos;
        this.schema = schema;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        buffer.writeInt(pos.getX());
        buffer.writeInt(pos.getY());
        buffer.writeInt(pos.getZ());

        try {
            if (schema != null) {
                writeString(schema.getKey(), buffer);
            } else {
                writeString("", buffer);
            }
        } catch (IOException exception) {
            System.err.println("An error occurred when writing schema name to packet buffer");
        }

    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        int x = buffer.readInt();
        int y = buffer.readInt();
        int z = buffer.readInt();
        pos = new BlockPos(x, y, z);

        try {
            String schemaKey = readString(buffer);
            schema = ItemUpgradeRegistry.instance.getSchema(schemaKey);
        } catch (IOException exception) {
            System.err.println("An error occurred when reading schema name from packet buffer");
        }
    }

    @Override
    public void handleClientSide(EntityPlayer player) {

    }

    @Override
    public void handleServerSide(EntityPlayer player) {
        TileEntityWorkbench workbench = (TileEntityWorkbench) player.world.getTileEntity(pos);
        if (workbench != null) {
            workbench.setCurrentSchema(schema, player);
        }
    }
}