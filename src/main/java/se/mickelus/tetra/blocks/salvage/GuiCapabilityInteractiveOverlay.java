package se.mickelus.tetra.blocks.salvage;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import se.mickelus.tetra.capabilities.CapabilityHelper;
import se.mickelus.tetra.gui.hud.GuiRootHud;

public class GuiCapabilityInteractiveOverlay extends GuiRootHud {


    public GuiCapabilityInteractiveOverlay() {

    }

    public void update(IBlockState blockState, EnumFacing face, EntityPlayer player) {
        clearChildren();
        if (blockState.getBlock() instanceof IBlockCapabilityInteractive) {
            IBlockCapabilityInteractive block = (IBlockCapabilityInteractive) blockState.getBlock();

            BlockInteraction[] interactions = block.getPotentialInteractions(blockState, face, CapabilityHelper.getPlayerCapabilities(player));

            for (BlockInteraction interaction : interactions) {
                GuiInteractiveOutline outline = new GuiInteractiveOutline(interaction);
                addChild(outline);
            }
        }

    }
}
