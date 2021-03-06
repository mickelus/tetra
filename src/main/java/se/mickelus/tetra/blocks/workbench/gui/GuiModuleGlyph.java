package se.mickelus.tetra.blocks.workbench.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.tetra.module.data.GlyphData;

public class GuiModuleGlyph extends GuiTexture {

    boolean shift = true;

    public GuiModuleGlyph(int x, int y, int width, int height, int tint, int textureX, int textureY, ResourceLocation textureLocation) {
        super(x, y, width + 1, height + 1, textureX, textureY, textureLocation);

        this.color = tint;
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
    public void draw(MatrixStack matrixStack, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        if (shift) {
            matrixStack.push();
            matrixStack.translate(0.5, 0.5, 0);
            drawTexture(matrixStack, textureLocation, refX + x, refY + y, width - 1, height - 1, textureX, textureY,
                    color, getOpacity() * opacity);
            matrixStack.pop();
        } else {
            drawTexture(matrixStack, textureLocation, refX + x, refY + y, width - 1, height - 1, textureX, textureY,
                    color, getOpacity() * opacity);
        }
    }
}