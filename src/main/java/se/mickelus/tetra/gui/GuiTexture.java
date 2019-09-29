package se.mickelus.tetra.gui;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;

public class GuiTexture extends GuiElement {

    private ResourceLocation textureLocation;

    private int textureX;
    private int textureY;

    protected int color = 0xffffff;

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

    public GuiTexture setTextureCoordinates(int x, int y) {
        textureX = x;
        textureY = y;
        return this;
    }

    public GuiTexture setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        super.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);

        GlStateManager.pushMatrix();
        Minecraft.getInstance().getTextureManager().bindTexture(textureLocation);

        GlStateManager.color(
            (color >> 16 & 255) / 255f,
            (color >> 8 & 255) / 255f,
            (color & 255) / 255f,
            opacity * getOpacity());
        GlStateManager.enableBlend();
        drawTexturedModalRect(
                refX + x,
                refY + y,
                textureX, textureY, width, height);
        GlStateManager.popMatrix();
    }
}
