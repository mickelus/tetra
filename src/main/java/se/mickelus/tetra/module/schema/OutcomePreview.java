package se.mickelus.tetra.module.schema;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.module.data.CapabilityData;
import se.mickelus.tetra.module.data.GlyphData;

public class OutcomePreview {
    public String key;
    public GlyphData glyph;
    public ItemStack itemStack;

    public SchemaType type;

    public CapabilityData capabilities;
    public ItemStack[] materials;

    public OutcomePreview(String key, GlyphData glyph, ItemStack itemStack, SchemaType type, CapabilityData capabilities, ItemStack[] materials) {
        this.key = key;
        this.glyph = glyph;
        this.itemStack = itemStack;
        this.type = type;
        this.capabilities = capabilities;
        this.materials = materials;
    }
}
