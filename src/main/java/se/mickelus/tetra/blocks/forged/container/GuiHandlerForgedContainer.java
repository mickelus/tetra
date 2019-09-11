package se.mickelus.tetra.blocks.forged.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import se.mickelus.tetra.blocks.workbench.TileEntityWorkbench;
import se.mickelus.tetra.blocks.workbench.gui.GuiWorkbench;
import se.mickelus.tetra.network.TetraGuiHandler;
import se.mickelus.tetra.util.CastOptional;

public class GuiHandlerForgedContainer implements TetraGuiHandler {

    @Override
    public Object getServerGuiElement(EntityPlayer player, World world, int x, int y, int z) {
        return getContainer(player, world, x, y, z);
    }

    @Override
    public Object getClientGuiElement(EntityPlayer player, World world, int x, int y, int z) {
            TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        return CastOptional.cast(tileEntity, TileEntityForgedContainer.class)
                .map(te -> new GuiForgedContainer(getContainer(player, world, x, y, z), te))
                .orElse(null);
    }

    private ContainerForgedContainer getContainer(EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        return CastOptional.cast(tileEntity, TileEntityForgedContainer.class)
                .map(te -> new ContainerForgedContainer(player.inventory, te, player))
                .orElse(null);
    }
}
