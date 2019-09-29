package se.mickelus.tetra.blocks.workbench;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.GuiTexture;
import se.mickelus.tetra.gui.hud.GuiRootHud;

@Deprecated
public class GuiHudWorkbench extends GuiRootHud {

    TileEntityWorkbench te;

    public GuiHudWorkbench() {
    }

    @Override
    protected void drawChildren(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        new GuiTexture(-16, -16, 31, 31, "textures/gui/glyphs.png").draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY, 1);

        GlStateManager.pushMatrix();
//        GlStateManager.depthMask(false);

        ItemStack itemStack = te.getStackInSlot(0);
        if (itemStack != null) {
            String itemName = itemStack.getDisplayName();
        }

//        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }

    public void setTileEntity(TileEntityWorkbench tileEntity) {
        this.te = tileEntity;
    }
}
