package se.mickelus.tetra.blocks.workbench;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import se.mickelus.tetra.network.BlockPosPacket;

public class WorkbenchPacketCraft extends BlockPosPacket {

    public WorkbenchPacketCraft() {}

    public WorkbenchPacketCraft(BlockPos pos) {
        super(pos);
    }

    @Override
    public void handle(PlayerEntity player) {
        WorkbenchTile workbench = (WorkbenchTile) player.level.getBlockEntity(pos);
        if (workbench != null) {
            workbench.craft(player);
        }
    }
}
