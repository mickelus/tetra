package se.mickelus.tetra.module.schematic;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.module.data.ToolData;
import se.mickelus.tetra.module.data.GlyphData;

public class OutcomePreview {
    public String key;
    public GlyphData glyph;
    public ItemStack itemStack;

    public SchematicType type;

    public ToolData tools;
    public ItemStack[] materials;

    public OutcomePreview(String key, GlyphData glyph, ItemStack itemStack, SchematicType type, ToolData tools, ItemStack[] materials) {
        this.key = key;
        this.glyph = glyph;
        this.itemStack = itemStack;
        this.type = type;
        this.tools = tools;
        this.materials = materials;
    }
}
