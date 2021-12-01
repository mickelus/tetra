package se.mickelus.tetra.blocks.workbench.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.tetra.module.data.GlyphData;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
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
    public void draw(PoseStack matrixStack, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        if (shift) {
            matrixStack.pushPose();
            matrixStack.translate(0.5, 0.5, 0);
            drawTexture(matrixStack, textureLocation, refX + x, refY + y, width - 1, height - 1, textureX, textureY,
                    color, getOpacity() * opacity);
            matrixStack.popPose();
        } else {
            drawTexture(matrixStack, textureLocation, refX + x, refY + y, width - 1, height - 1, textureX, textureY,
                    color, getOpacity() * opacity);
        }
    }
}