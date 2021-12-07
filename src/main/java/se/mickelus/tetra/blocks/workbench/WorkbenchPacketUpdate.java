package se.mickelus.tetra.blocks.workbench;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;
import se.mickelus.mutil.network.AbstractPacket;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
@ParametersAreNonnullByDefault
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
    public void toBytes(FriendlyByteBuf buffer) {
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
    public void fromBytes(FriendlyByteBuf buffer) {
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
    public void handle(Player player) {
        WorkbenchTile workbench = (WorkbenchTile) player.level.getBlockEntity(pos);
        if (workbench != null) {
            workbench.update(schematic, selectedSlot, player);
        }
    }
}
