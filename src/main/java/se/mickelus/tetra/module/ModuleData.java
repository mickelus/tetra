package se.mickelus.tetra.module;

import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;

public class ModuleData {
    public String key;

    public Priority namePriority = Priority.BASE;
    public Priority prefixPriority = Priority.BASE;

    public String material;
    public int materialData = -1;
    public int materialCount = 1;

    public int durability = 0;
    public float durabilityMultiplier = 1;

    public int integrity = 0;
    public float integrityMultiplier = 1;

    int size = 0;

    public GlyphData glyph = new GlyphData();

    public CapabilityData capabilities = new CapabilityData();
    public CapabilityData requiredCapabilities = new CapabilityData();

    public ModuleData() {}

    public ResourceLocation getTextureLocation() {
        return new ResourceLocation(TetraMod.MOD_ID, "items/" + key);
    }
}
