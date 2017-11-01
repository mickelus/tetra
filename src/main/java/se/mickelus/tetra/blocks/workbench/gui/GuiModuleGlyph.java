package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.gui.GuiElement;

public class GuiModuleGlyph extends GuiElement {

    private ResourceLocation textureLocation;

    int textureX;
    int textureY;
    int tint;

    public GuiModuleGlyph(int x, int y, int width, int height, int tint, int textureX, int textureY, ResourceLocation textureLocation) {
        super(x, y, width + 1, height + 1);

        this.tint = tint;
        this.textureX = textureX;
        this.textureY = textureY;

        this.textureLocation = textureLocation;
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        super.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY);

        Minecraft.getMinecraft().getTextureManager().bindTexture(textureLocation);

        float red = (float)(tint >> 16 & 255) / 255.0F;
        float blue = (float)(tint >> 8 & 255) / 255.0F;
        float green = (float)(tint & 255) / 255.0F;

        GlStateManager.translate(0.5F, 0.5F, 0);
        GlStateManager.color(red, blue, green, 1);
        drawTexturedModalRect(
                refX + x,
                refY + y,
                textureX, textureY, width - 1, height - 1);
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.translate(-0.5F, -0.5F, 0);
    }
}