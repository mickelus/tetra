package se.mickelus.tetra.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

public class GuiRoot extends GuiNode {

    protected Minecraft mc;

    public GuiRoot(Minecraft mc) {
        this.mc = mc;
    }

    public void draw() {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int width = scaledResolution.getScaledWidth();
        int height = scaledResolution.getScaledHeight();
        int mouseX = Mouse.getX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getY() * height / mc.displayHeight - 1;
        drawChildren(0,0, width, height, mouseX, mouseY, 1);
    }

}
