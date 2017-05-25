package se.mickelus.tetra.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;

public class GuiTextureOffset extends GuiElement {

    private ResourceLocation textureLocation;

    public GuiTextureOffset(int x, int y, int width, int height, String texture) {
        super(x, y, width + 1, height + 1);

        textureLocation = new ResourceLocation(TetraMod.MOD_ID, texture);
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        super.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY);

        Minecraft.getMinecraft().getTextureManager().bindTexture(textureLocation);

        GlStateManager.translate(0.5F, 0.5F, 0);
        drawTexturedModalRect(
                refX + x,
                refY + y,
                0, 0, width - 1, height - 1);
        GlStateManager.translate(-0.5F, -0.5F, 0);
    }
}
