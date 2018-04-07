package se.mickelus.tetra.module.data;

import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.ItemEffect;
import se.mickelus.tetra.module.Priority;

public class ModuleData {
    public String key;

    public Priority namePriority = Priority.BASE;
    public Priority prefixPriority = Priority.BASE;

    public String material;
    public int materialData = -1;
    public int materialCount = 1;

    public float damage = 0;
    public float damageMultiplier = 1;

    public float attackSpeed = 0;
    public float attackSpeedMultiplier = 1;

    public int durability = 0;
    public float durabilityMultiplier = 1;

    public int integrity = 0;
    public float integrityMultiplier = 1;

    public EffectData effects = new EffectData();

    public int size = 0;

    public GlyphData glyph = new GlyphData();

    public CapabilityData capabilities = new CapabilityData();
    public CapabilityData requiredCapabilities = new CapabilityData();

    public ModuleData() {}

    public ResourceLocation getTextureLocation() {
        return new ResourceLocation(TetraMod.MOD_ID, "items/" + key);
    }
}
