package se.mickelus.tetra.module.schema;

import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.util.Filter;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

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
 *                 "item": "minecraft:oak_planks",
 *                 "count": 2
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
 *                 "type": "minecraft:tag",
 *                 "name": "forge:ingots/steeleaf",
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
     * Marks if this should replace or merge with existing entries (if any) for this schema definition. The default behaviour for upgrade
     * schemas added by other mods and datapacks is to merge values set by those into the existing entry (if any).
     * By setting replace to true it's possible to completely replace schemas registered by tetra, which can be useful when one wants to
     * remove something.
     */
    public boolean replace = false;

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
     * Defines if this schema should only be applicable when the player has a honing attempt available for the item.
     */
    public boolean hone = false;

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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GENERATED FIELDS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * The id for the schema, should be unique. This is automatically set based on the schema definition's location within the resource
     * directory structure.
     */
    public String key;

    private static final SchemaDefinition defaultValues = new SchemaDefinition();

    public static void copyFields(SchemaDefinition from, SchemaDefinition to) {
        to.slots = Stream.concat(Arrays.stream(to.slots), Arrays.stream(from.slots))
                .distinct()
                .toArray(String[]::new);

        to.keySuffixes = Stream.concat(Arrays.stream(to.keySuffixes), Arrays.stream(from.keySuffixes))
                .distinct()
                .toArray(String[]::new);

        if (!Objects.equals(from.localizationKey, defaultValues.localizationKey)) {
            to.localizationKey = from.localizationKey;
        }


        if (from.materialSlotCount != defaultValues.materialSlotCount) {
            to.materialSlotCount = from.materialSlotCount;
        }


        if (from.repair != defaultValues.repair) {
            to.repair = from.repair;
        }


        if (from.hone != defaultValues.hone) {
            to.hone = from.hone;
        }


        if (from.requirement != defaultValues.requirement) {
            to.requirement = from.requirement;
        }


        if (from.materialRevealSlot != defaultValues.materialRevealSlot) {
            to.materialRevealSlot = from.materialRevealSlot;
        }


        if (from.displayType != defaultValues.displayType) {
            to.displayType = from.displayType;
        }


        if (from.rarity != defaultValues.rarity) {
            to.rarity = from.rarity;
        }


        if (from.glyph != defaultValues.glyph) {
            to.glyph = from.glyph;
        }

        to.outcomes = Stream.concat(Arrays.stream(to.outcomes), Arrays.stream(from.outcomes))
                .filter(Filter.distinct(outcome -> outcome.material))
                .toArray(OutcomeDefinition[]::new);
    }
}
