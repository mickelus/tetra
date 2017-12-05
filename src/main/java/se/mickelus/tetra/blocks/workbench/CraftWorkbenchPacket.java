package se.mickelus.tetra.blocks.workbench;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import se.mickelus.tetra.network.BlockPosPacket;

public class CraftWorkbenchPacket extends BlockPosPacket {

    public CraftWorkbenchPacket() {}

    public CraftWorkbenchPacket(BlockPos pos) {
        super(pos);
    }

    @Override
    public void handleClientSide(EntityPlayer player) {

    }

    @Override
    public void handleServerSide(EntityPlayer player) {
        TileEntityWorkbench workbench = (TileEntityWorkbench) player.world.getTileEntity(pos);
        if (workbench != null) {
            workbench.craft(player);
        }
    }
}
