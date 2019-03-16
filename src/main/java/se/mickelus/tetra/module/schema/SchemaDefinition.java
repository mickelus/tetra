package se.mickelus.tetra.module.schema;

import net.minecraft.advancements.critereon.ItemPredicate;
import se.mickelus.tetra.module.data.GlyphData;

/**
 * Schemas define how players craft new modules, and which materials yield which module variant.
 * Example json:
 * {
 *     "key": "basic_blade_schema",
 *     "slots": ["sword/blade"],
 *     "materialSlotCount": 1,
 *     "displayType": "major",
 *     "outcomes": [
 *         {
 *             "material": {
 *                 "item": "minecraft:planks",
 *                 "count": 2,
 *                 "data": 4
 *             },
 *             "requiredCapabilities": {
 *                 "axe": 1,
 *                 "hammer": 4
 *             },
 *             "moduleKey": "sword/basic_blade",
 *             "moduleVariant": "basic_blade/acacia"
 *         },
 *         {
 *             "material": {
 *                 "type": "forge:ore_dict",
 *                 "ore": "ingotSteeleaf",
 *                 "count": 2
 *             },
 *             "requiredCapabilities": {
 *                 "hammer": 2
 *             },
 *             "moduleKey": "sword/basic_blade",
 *             "moduleVariant": "basic_blade/steeleaf",
 *             "improvements": {
 *                 "enchantment/looting": 2
 *             }
 *         }
 *     ]
 * }
 */
public class SchemaDefinition {

    /**
     * The id for the schema, should be unique.
     */
    public String key;

    /**
     * The key used for localized human readable strings such as name and description. If this is not available the
     * key field is used for picking up localized strings instead, which is preferable in most cases. This field is
     * useful when multiple schemas should use the same name, description etc.
     */
    public String localizationKey;

    /**
     * An array of slots which this schema is applicable for.
     */
    public String[] slots = new String[0];

    /**
     * Suffixes, used when crafted modules should have different keys depending on slot (e.g. pickaxe head keys end with
     * "_left" or "_right" so that different textures can be used depending on the slot. Optional, but if provided it
     * has to have the same length as the slots field.
     */
    public String[] keySuffixes = new String[0];

    /**
     * The number of material that can be used when crafting with this schema, currently only a value 0 or 1 is
     * supported.
     */
    public int materialSlotCount = 0;

    /**
     * States if a repair schema should also be generated based on this schema. Useful when the crafted module
     * is repaired using the same materials and capabilities as if it was crafted.
     */
    public boolean repair = true;

    /**
     * An item predicate which has to be met for this schema to be applicable. Will not show up in the schema list
     * if this is not met. Optional, allows all items by default (as long as they have the required slots)
     */
    public ItemPredicate requirement = ItemPredicate.ANY;

    /**
     * If set this schema will only be visible if the player carries at least one itemstack that will produce an outcome if placed in the slot at the
     * provided index.
     */
    public int materialRevealSlot = -1;

    /**
     * Defines the outline around the schema glyph, visible in most views where players interact with the schema
     * somehow. Has four possible values: "minor", "major", "improvement", "other". Minor and major display a similar
     * outline as minor and major modules, "improvement" looks like a major module outline with a plus, "other" has no
     * outline and the outline can instead be part of the glyph.
     */
    public SchemaType displayType = SchemaType.other;

    /**
     * The rarity of a schema affects how it is rendered. Colors and effects are used to differentiate between effects.
     */
    public SchemaRarity rarity = SchemaRarity.basic;

    /**
     * The glyph displayed for this schema, preferably the same as the module it will be used to craft but with no tint.
     */
    public GlyphData glyph = new GlyphData();

    /**
     * An array of all potential outcomes of this schema.
     */
    public OutcomeDefinition[] outcomes = new OutcomeDefinition[0];
}
