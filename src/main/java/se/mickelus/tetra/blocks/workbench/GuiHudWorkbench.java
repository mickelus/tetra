package se.mickelus.tetra.blocks.workbench;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.GuiTexture;
import se.mickelus.tetra.gui.hud.GuiRootHud;

@Deprecated
public class GuiHudWorkbench extends GuiRootHud {

    TileEntityWorkbench te;

    public GuiHudWorkbench() {
        super(Minecraft.getMinecraft());
    }

    @Override
    protected void drawChildren(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        new GuiTexture(-16, -16, 31, 31, "textures/gui/glyphs.png").draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY);

        GlStateManager.pushMatrix();
//        GlStateManager.depthMask(false);

        ItemStack itemStack = te.getStackInSlot(0);
        if (itemStack != null) {
            String itemName = itemStack.getDisplayName();
            int width = mc.fontRenderer.getStringWidth(itemName);
            mc.fontRenderer.drawString(itemName, - width / 2, -30, 0xffffffff);
        }

//        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }

    public void setTileEntity(TileEntityWorkbench tileEntity) {
        this.te = tileEntity;
    }
}
