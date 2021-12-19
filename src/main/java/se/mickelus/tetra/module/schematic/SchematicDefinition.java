package se.mickelus.tetra.module.schematic;

import net.minecraft.advancements.critereon.ItemPredicate;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.data.MaterialMultiplier;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class SchematicDefinition {

    private static final SchematicDefinition defaultValues = new SchematicDefinition();
    /**
     * Marks if this should replace or merge with existing entries (if any) for this schematic definition. The default behaviour for upgrade
     * schematics added by other mods and datapacks is to merge values set by those into the existing entry (if any).
     * By setting replace to true it's possible to completely replace schematics registered by tetra, which can be useful when one wants to
     * remove something.
     */
    public boolean replace = false;
    /**
     * The key used for localized human readable strings such as name and description. If this is not available the
     * key field is used for picking up localized strings instead, which is preferable in most cases. This field is
     * useful when multiple schematics should use the same name, description etc.
     */
    public String localizationKey;
    /**
     * An array of slots which this schematic is applicable for.
     */
    public String[] slots = new String[0];
    /**
     * Suffixes, used when crafted modules should have different keys depending on slot (e.g. pickaxe head keys end with
     * "_left" or "_right" so that different textures can be used depending on the slot. Optional, but if provided it
     * has to have the same length as the slots field.
     */
    public String[] keySuffixes = new String[0];
    /**
     * The number of material that can be used when crafting with this schematic, currently only a value 0 or 1 is
     * supported.
     */
    public int materialSlotCount = 0;
    /**
     * States if a repair schematic should also be generated based on this schematic. Useful when the crafted module
     * is repaired using the same materials and tools as if it was crafted.
     */
    public boolean repair = true;
    /**
     * Defines if this schematic should only be applicable when the player has a honing attempt available for the item.
     */
    public boolean hone = false;
    /**
     * An item predicate which has to be met for this schematic to be applicable. Will not show up in the schematic list
     * if this is not met. Optional, allows all items by default (as long as they have the required slots)
     */
    public ItemPredicate requirement = ItemPredicate.ANY;
    /**
     * If true this schematic will only be visible if it is unlocked, either by the workbench or by a nearby block.
     */
    public boolean locked = false;
    /**
     * If set this schematic will only be visible if the player carries at least one itemstack that will produce an outcome if placed in the slot at the
     * provided index.
     */
    public int materialRevealSlot = -1;
    /**
     * Defines the outline around the schematic glyph, visible in most views where players interact with the schematic
     * somehow. Has four possible values: "minor", "major", "improvement", "other". Minor and major display a similar
     * outline as minor and major modules, "improvement" looks like a major module outline with a plus, "other" has no
     * outline and the outline can instead be part of the glyph.
     */
    public SchematicType displayType = SchematicType.other;
    /**
     * The rarity of a schematic affects how it is rendered. Colors and effects are used to differentiate between effects.
     */
    public SchematicRarity rarity = SchematicRarity.basic;
    /**
     * The glyph displayed for this schematic, preferably the same as the module it will be used to craft but with no tint.
     */
    public GlyphData glyph = new GlyphData();
    /**
     * Used to display some information about how material properties translate into stats for the module or improvement that the schematic crafts.
     */
    public MaterialMultiplier translation;
    /**
     * Used to display hints about which materials that can be used for this schematic, strings starting with # are considered as materials, others
     * as item IDs.
     */
    public String[] applicableMaterials;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GENERATED FIELDS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * An array of all potential outcomes of this schematic.
     */
    public OutcomeDefinition[] outcomes = new OutcomeDefinition[0];
    /**
     * The id for the schematic, should be unique. This is automatically set based on the schematic definition's location within the resource
     * directory structure.
     */
    public String key;

    public static void copyFields(SchematicDefinition from, SchematicDefinition to) {
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


        if (!from.requirement.equals(defaultValues.requirement)) {
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


        if (!from.glyph.equals(defaultValues.glyph)) {
            to.glyph = from.glyph;
        }

        if (to.applicableMaterials != null && from.applicableMaterials != null) {
            to.applicableMaterials = Stream.concat(Arrays.stream(to.applicableMaterials), Arrays.stream(from.applicableMaterials))
                    .toArray(String[]::new);
        } else if (from.applicableMaterials != null) {
            to.applicableMaterials = from.applicableMaterials;
        }

        to.outcomes = Stream.concat(Arrays.stream(to.outcomes), Arrays.stream(from.outcomes))
                .toArray(OutcomeDefinition[]::new);
    }
}
