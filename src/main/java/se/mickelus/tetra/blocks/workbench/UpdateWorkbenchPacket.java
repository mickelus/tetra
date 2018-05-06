package se.mickelus.tetra.blocks.workbench;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.schema.UpgradeSchema;
import se.mickelus.tetra.network.AbstractPacket;

import java.io.IOException;

public class UpdateWorkbenchPacket extends AbstractPacket {

    private BlockPos pos;
    private UpgradeSchema schema;
    private String selectedSlot;

    public UpdateWorkbenchPacket() {}

    public UpdateWorkbenchPacket(BlockPos pos, UpgradeSchema schema, String selectedSlot) {
        this.pos = pos;
        this.schema = schema;
        this.selectedSlot = selectedSlot;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(pos.getX());
        buffer.writeInt(pos.getY());
        buffer.writeInt(pos.getZ());

        try {
            if (schema != null) {
                writeString(schema.getKey(), buffer);
            } else {
                writeString("", buffer);
            }

            if (selectedSlot != null) {
                writeString(selectedSlot, buffer);
            } else {
                writeString("", buffer);
            }
        } catch (IOException exception) {
            System.err.println("An error occurred when writing schema name to packet buffer");
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        int x = buffer.readInt();
        int y = buffer.readInt();
        int z = buffer.readInt();
        pos = new BlockPos(x, y, z);

        try {
            String schemaKey = readString(buffer);
            schema = ItemUpgradeRegistry.instance.getSchema(schemaKey);

            selectedSlot = readString(buffer);

            if ("".equals(selectedSlot)) {
                selectedSlot = null;
            }
        } catch (IOException exception) {
            System.err.println("An error occurred when reading schema name from packet buffer");
        }
    }

    @Override
    public void handle(EntityPlayer player) {
        TileEntityWorkbench workbench = (TileEntityWorkbench) player.world.getTileEntity(pos);
        if (workbench != null) {
            workbench.update(schema, selectedSlot, player);
        }
    }
}
