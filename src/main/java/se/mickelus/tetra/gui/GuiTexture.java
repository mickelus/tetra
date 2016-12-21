package se.mickelus.tetra.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;

public class GuiTexture extends GuiElement {

    private ResourceLocation textureLocation;

    public GuiTexture(int x, int y, int width, int height, String texture) {
        super(x, y, width, height);

        textureLocation = new ResourceLocation(TetraMod.MOD_ID, texture);
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        super.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY);

        Minecraft.getMinecraft().getTextureManager().bindTexture(textureLocation);

        // draw background
        drawTexturedModalRect(
                refX + x,
                refY + y,
                0, 0, width, height);
    }
}
