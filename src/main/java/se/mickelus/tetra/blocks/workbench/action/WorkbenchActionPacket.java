package se.mickelus.tetra.blocks.workbench.action;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import se.mickelus.tetra.blocks.workbench.TileEntityWorkbench;
import se.mickelus.tetra.network.BlockPosPacket;

import java.io.IOException;

public class WorkbenchActionPacket extends BlockPosPacket {

    private String actionKey;

    public WorkbenchActionPacket() {}

    public WorkbenchActionPacket(BlockPos pos, String actionKey) {
        super(pos);
        this.actionKey = actionKey;
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        super.toBytes(buffer);
        try {
            writeString(actionKey, buffer);
        } catch (IOException exception) {
            System.err.println("An error occurred when writing action name to packet buffer");
        }
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        super.fromBytes(buffer);

        try {
            actionKey = readString(buffer);
        } catch (IOException exception) {
            System.err.println("An error occurred when reading action name from packet buffer");
        }
    }

    @Override
    public void handle(PlayerEntity player) {
        TileEntityWorkbench workbench = (TileEntityWorkbench) player.world.getTileEntity(pos);
        if (workbench != null) {
            workbench.performAction(player, actionKey);
        }
    }
}
