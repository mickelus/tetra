package se.mickelus.tetra.blocks.workbench.action;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
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
    public void toBytes(ByteBuf buffer) {
        super.toBytes(buffer);
        try {
            writeString(actionKey, buffer);
        } catch (IOException exception) {
            System.err.println("An error occurred when writing action name to packet buffer");
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        super.fromBytes(buffer);

        try {
            actionKey = readString(buffer);
        } catch (IOException exception) {
            System.err.println("An error occurred when reading action name from packet buffer");
        }
    }

    @Override
    public void handle(EntityPlayer player) {
        TileEntityWorkbench workbench = (TileEntityWorkbench) player.world.getTileEntity(pos);
        if (workbench != null) {
            workbench.performAction(player, actionKey);
        }
    }
}
