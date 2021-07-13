package se.mickelus.tetra.module.schematic;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.module.data.ToolData;
import se.mickelus.tetra.module.data.GlyphData;

public class OutcomePreview {
    public String moduleKey;
    public String variantName;
    public String variantKey;
    public String category;
    public GlyphData glyph;
    public ItemStack itemStack;

    public SchematicType type;

    public ToolData tools;
    public ItemStack[] materials;

    public OutcomePreview(String moduleKey, String variantKey, String variantName, String category, GlyphData glyph, ItemStack itemStack,
            SchematicType type, ToolData tools, ItemStack[] materials) {
        this.moduleKey = moduleKey;
        this.variantKey = variantKey;
        this.variantName = variantName.toLowerCase();
        this.category = category;
        this.glyph = glyph;
        this.itemStack = itemStack;
        this.type = type;
        this.tools = tools;
        this.materials = materials;
    }
}
