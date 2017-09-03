package se.mickelus.tetra.blocks.workbench;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import se.mickelus.tetra.items.toolbelt.ContainerToolbelt;
import se.mickelus.tetra.items.toolbelt.GuiToolbelt;
import se.mickelus.tetra.network.TetraGuiHandler;

public class GuiHandlerWorkbench implements TetraGuiHandler {

    public static final int GUI_WORKBENCH_ID = 1;

    @Override
    public Object getServerGuiElement(EntityPlayer player, World world, int x, int y, int z) {
        return getContainer(player, world, x, y, z);
    }

    @Override
    public Object getClientGuiElement(EntityPlayer player, World world, int x, int y, int z) {
            TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
            if (tileEntity != null && tileEntity instanceof TileEntityWorkbench) {
                return new GuiWorkbench(getContainer(player, world, x, y, z), (TileEntityWorkbench) tileEntity, player);
            }
            return null;
    }

    private ContainerWorkbench getContainer(EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

        if (tileEntity != null && tileEntity instanceof TileEntityWorkbench) {
            return new ContainerWorkbench(player.inventory, (TileEntityWorkbench) tileEntity, player);
        }
        return null;
    }
}
