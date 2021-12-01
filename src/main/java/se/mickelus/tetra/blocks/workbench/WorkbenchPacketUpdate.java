package se.mickelus.tetra.blocks.workbench;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;
import se.mickelus.tetra.network.AbstractPacket;

import java.io.IOException;

public class WorkbenchPacketUpdate extends AbstractPacket {

    private BlockPos pos;
    private UpgradeSchematic schematic;
    private String selectedSlot;

    public WorkbenchPacketUpdate() {}

    public WorkbenchPacketUpdate(BlockPos pos, UpgradeSchematic schematic, String selectedSlot) {
        this.pos = pos;
        this.schematic = schematic;
        this.selectedSlot = selectedSlot;
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(pos.getX());
        buffer.writeInt(pos.getY());
        buffer.writeInt(pos.getZ());

        try {
            if (schematic != null) {
                writeString(schematic.getKey(), buffer);
            } else {
                writeString("", buffer);
            }

            if (selectedSlot != null) {
                writeString(selectedSlot, buffer);
            } else {
                writeString("", buffer);
            }
        } catch (IOException exception) {
            System.err.println("An error occurred when writing schematic name to packet buffer");
        }
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        int x = buffer.readInt();
        int y = buffer.readInt();
        int z = buffer.readInt();
        pos = new BlockPos(x, y, z);

        try {
            String schematicKey = readString(buffer);
            schematic = SchematicRegistry.getSchematic(schematicKey);

            selectedSlot = readString(buffer);

            if ("".equals(selectedSlot)) {
                selectedSlot = null;
            }
        } catch (IOException exception) {
            System.err.println("An error occurred when reading schematic name from packet buffer");
        }
    }

    @Override
    public void handle(PlayerEntity player) {
        WorkbenchTile workbench = (WorkbenchTile) player.level.getBlockEntity(pos);
        if (workbench != null) {
            workbench.update(schematic, selectedSlot, player);
        }
    }
}
