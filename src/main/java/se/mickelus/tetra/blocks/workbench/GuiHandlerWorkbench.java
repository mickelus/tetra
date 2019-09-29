package se.mickelus.tetra.blocks.workbench;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import se.mickelus.tetra.blocks.workbench.gui.GuiWorkbench;
import se.mickelus.tetra.network.TetraGuiHandler;

public class GuiHandlerWorkbench implements TetraGuiHandler {
    @Override
    public Object getServerGuiElement(PlayerEntity player, World world, int x, int y, int z) {
        return getContainer(player, world, x, y, z);
    }

    @Override
    public Object getClientGuiElement(PlayerEntity player, World world, int x, int y, int z) {
            TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
            if (tileEntity != null && tileEntity instanceof TileEntityWorkbench) {
                return new GuiWorkbench(getContainer(player, world, x, y, z), (TileEntityWorkbench) tileEntity, player);
            }
            return null;
    }

    private ContainerWorkbench getContainer(PlayerEntity player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

        if (tileEntity != null && tileEntity instanceof TileEntityWorkbench) {
            return new ContainerWorkbench(player.inventory, (TileEntityWorkbench) tileEntity, player);
        }
        return null;
    }
}
