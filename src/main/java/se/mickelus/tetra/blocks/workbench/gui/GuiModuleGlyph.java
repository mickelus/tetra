package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.module.data.GlyphData;

public class GuiModuleGlyph extends GuiElement {

    private ResourceLocation textureLocation;

    int textureX;
    int textureY;
    int tint;

    boolean shift = true;

    public GuiModuleGlyph(int x, int y, int width, int height, int tint, int textureX, int textureY, ResourceLocation textureLocation) {
        super(x, y, width + 1, height + 1);

        this.tint = tint;
        this.textureX = textureX;
        this.textureY = textureY;

        this.textureLocation = textureLocation;
    }

    public GuiModuleGlyph(int x, int y, int width, int height, int tint, GlyphData glyphData) {
        this(x, y, width, height, tint, glyphData.textureX, glyphData.textureY, glyphData.textureLocation);
    }

    public GuiModuleGlyph(int x, int y, int width, int height, GlyphData glyphData) {
        this(x, y, width, height, glyphData.tint, glyphData.textureX, glyphData.textureY, glyphData.textureLocation);
    }

    public GuiModuleGlyph setShift(boolean shift) {
        this.shift = shift;
        return this;
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        super.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);

        Minecraft.getInstance().getTextureManager().bindTexture(textureLocation);

        float red = (float)(tint >> 16 & 255) / 255.0F;
        float blue = (float)(tint >> 8 & 255) / 255.0F;
        float green = (float)(tint & 255) / 255.0F;

        if (shift) {
            GlStateManager.translate(0.5F, 0.5F, 0);
        }

        GlStateManager.color(red, blue, green, opacity * this.opacity);
        drawTexturedModalRect(
                refX + x,
                refY + y,
                textureX, textureY, width - 1, height - 1);
        GlStateManager.color(1, 1, 1, 1);

        if (shift) {
            GlStateManager.translate(-0.5F, -0.5F, 0);
        }
    }
}