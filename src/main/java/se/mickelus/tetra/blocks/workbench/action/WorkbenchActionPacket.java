package se.mickelus.tetra.blocks.workbench.action;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.network.BlockPosPacket;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
@ParametersAreNonnullByDefault
public class WorkbenchActionPacket extends BlockPosPacket {

    private String actionKey;

    public WorkbenchActionPacket() {}

    public WorkbenchActionPacket(BlockPos pos, String actionKey) {
        super(pos);
        this.actionKey = actionKey;
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        super.toBytes(buffer);
        try {
            writeString(actionKey, buffer);
        } catch (IOException exception) {
            System.err.println("An error occurred when writing action name to packet buffer");
        }
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        super.fromBytes(buffer);

        try {
            actionKey = readString(buffer);
        } catch (IOException exception) {
            System.err.println("An error occurred when reading action name from packet buffer");
        }
    }

    @Override
    public void handle(Player player) {
        WorkbenchTile workbench = (WorkbenchTile) player.level.getBlockEntity(pos);
        if (workbench != null) {
            workbench.performAction(player, actionKey);
        }
    }
}
