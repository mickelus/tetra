package se.mickelus.tetra.module.data;

import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;

public class GlyphData {
    public ResourceLocation textureLocation = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/glyphs.png");
    public int tint = 0xffffffff;
    public int textureX = 0;
    public int textureY = 0;

    public GlyphData() {}

    public GlyphData(int textureX, int textureY) {
        this.textureX = textureX;
        this.textureY = textureY;
    }

    public GlyphData(String texture, int textureX, int textureY) {
        textureLocation = new ResourceLocation(TetraMod.MOD_ID, texture);
        this.textureX = textureX;
        this.textureY = textureY;
    }
}
