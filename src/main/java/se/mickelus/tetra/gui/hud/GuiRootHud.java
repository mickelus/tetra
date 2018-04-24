package se.mickelus.tetra.gui.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import se.mickelus.tetra.gui.GuiElement;

public class GuiRootHud extends GuiElement {
    protected Minecraft mc;

    public GuiRootHud(Minecraft mc) {
        super(0, 0, 0, 0);
        this.mc = mc;
    }

    public void draw() {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5, 1.5, 0.5);

        GlStateManager.scale(1/128f, -1/128f, 1/128f);

        GlStateManager.translate(0.5, 0.5, 0);
        GlStateManager.glNormal3f(0.0F, 0.0F, -0.010416667F);
        GlStateManager.disableLighting();

        drawChildren(0,0, 0, 0, 0, 0, 1);

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
