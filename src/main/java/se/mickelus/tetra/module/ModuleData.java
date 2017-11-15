package se.mickelus.tetra.module;

import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;

public class ModuleData {
    public String key;

    public String material;

    public int durability = 0;
    public float durabilityMultiplier = 1;

    public int integrity = 0;
    public float integrityMultiplier = 1;

    public GlyphData glyph = new GlyphData();

    public ModuleData() {}

    public ResourceLocation getTextureLocation() {
        return new ResourceLocation(TetraMod.MOD_ID, "items/" + key);
    }
}
