package se.mickelus.tetra.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;

public class GuiTexture extends GuiElement {

    private ResourceLocation textureLocation;

    private final int textureX;
    private final int textureY;

    public GuiTexture(int x, int y, int width, int height, String texture) {
        this(x, y, width, height, new ResourceLocation(TetraMod.MOD_ID, texture));
    }

    public GuiTexture(int x, int y, int width, int height, int textureX, int textureY, String texture) {
        this(x, y, width, height, textureX, textureY, new ResourceLocation(TetraMod.MOD_ID, texture));
    }

    public GuiTexture(int x, int y, int width, int height, ResourceLocation textureLocation) {
        this(x, y, width, height, 0, 0, textureLocation);
    }

    public GuiTexture(int x, int y, int width, int height, int textureX, int textureY, ResourceLocation textureLocation) {
        super(x, y, width, height);

        this.textureX = textureX;
        this.textureY = textureY;

        this.textureLocation = textureLocation;
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        super.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY);

        Minecraft.getMinecraft().getTextureManager().bindTexture(textureLocation);

        GlStateManager.enableBlend();
        drawTexturedModalRect(
                refX + x,
                refY + y,
                textureX, textureY, width, height);
        GlStateManager.disableBlend();
    }
}
