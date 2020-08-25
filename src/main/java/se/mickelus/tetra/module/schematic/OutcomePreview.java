package se.mickelus.tetra.module.schematic;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.module.data.ToolData;
import se.mickelus.tetra.module.data.GlyphData;

public class OutcomePreview {
    public String key;
    public String category;
    public GlyphData glyph;
    public ItemStack itemStack;

    public SchematicType type;

    public ToolData tools;
    public ItemStack[] materials;

    public OutcomePreview(String key, String category, GlyphData glyph, ItemStack itemStack, SchematicType type, ToolData tools, ItemStack[] materials) {
        this.key = key;
        this.category = category;
        this.glyph = glyph;
        this.itemStack = itemStack;
        this.type = type;
        this.tools = tools;
        this.materials = materials;
    }
}
