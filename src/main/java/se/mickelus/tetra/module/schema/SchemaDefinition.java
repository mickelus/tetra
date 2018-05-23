package se.mickelus.tetra.module.schema;

import net.minecraft.advancements.critereon.ItemPredicate;
import se.mickelus.tetra.module.data.GlyphData;


public class SchemaDefinition {
    public String key = "";
    public String[] slots = new String[0];
    public int materialSlotCount = 0;
    public ItemPredicate requirement = ItemPredicate.ANY;
    public SchemaType displayType = SchemaType.other;
    public GlyphData glyph = new GlyphData();
    public OutcomeDefinition[] outcomes = new OutcomeDefinition[0];
}
