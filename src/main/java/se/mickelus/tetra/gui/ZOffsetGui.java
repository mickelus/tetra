package se.mickelus.tetra.gui;

import net.minecraft.client.gui.GuiGraphics;
import se.mickelus.mutil.gui.GuiElement;

public class ZOffsetGui extends GuiElement {
    protected double z;

    public ZOffsetGui(int x, int y, double z) {
        super(x, y, 0, 0);
        this.z = z;
    }

    @Override
    protected void drawChildren(GuiGraphics graphics, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, z);
        super.drawChildren(graphics, refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);
        graphics.pose().translate(0, 0, -z);
        graphics.pose().popPose();
    }
}
