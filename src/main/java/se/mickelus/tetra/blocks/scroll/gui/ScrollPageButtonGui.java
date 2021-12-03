package se.mickelus.tetra.blocks.scroll.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import se.mickelus.mutil.gui.GuiClickable;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.tetra.TetraMod;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class ScrollPageButtonGui extends GuiClickable {
    GuiTexture regularTexture;
    GuiTexture hoverTexture;

    static final ResourceLocation texture = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/pamphlet.png");

    public ScrollPageButtonGui(int x, int y, boolean back, Runnable onClick) {
        super(x, y, 25, 14, onClick);

        if (back) {
            regularTexture = new GuiTexture(0, 0, width, height, 0, 205, texture);
            hoverTexture = new GuiTexture(0, 0, width, height, 23, 205, texture);
        } else {
            regularTexture = new GuiTexture(0, 0, width, height, 0, 191, texture);
            hoverTexture = new GuiTexture(0, 0, width, height, 23, 191, texture);
        }
    }

    @Override
    protected void drawChildren(PoseStack matrixStack, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        if (hasFocus()) {
            hoverTexture.draw(matrixStack, refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);
        } else {
            regularTexture.draw(matrixStack, refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);
        }
    }
}
