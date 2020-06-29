package se.mickelus.tetra.module.data;

import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.gui.GuiTextures;

/**
 * Used to describe glyphs for modules and schemas in the UI.
 *
 * Example json:
 * {
 *     "tint": "9d804e",
 *     "textureY": 32,
 *     "textureX": 48
 * }
 */
public class GlyphData {

    /**
     * Color tint for the glyph, used to color glyphs based on materials or perhaps some other attribute. Expressed
     * as a hexadecimal string.
     *
     * Json format: "rrggbb"
     */
    public int tint = 0xffffffff;

    /**
     * Glyph offset in sprite sheet. The default glyph texture contains multiple glyphs and these are the coordinates
     * to the desired glyph within the sheet.
     */
    public int textureX = 0;
    public int textureY = 0;

    /**
     * The resourcelocation for the glyph texture. This is optional and most glyphs are available in the defailt
     * texture.
     *
     * Json format: "domain:path"
     */
    public ResourceLocation textureLocation = GuiTextures.glyphs;

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

    public GlyphData(ResourceLocation textureLocation, int textureX, int textureY) {
        this.textureLocation = textureLocation;
        this.textureX = textureX;
        this.textureY = textureY;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GlyphData
                && textureX == ((GlyphData) obj).textureX
                && textureY == ((GlyphData) obj).textureY
                && tint == ((GlyphData) obj).tint
                && textureLocation.equals(((GlyphData) obj).textureLocation);
    }
}
