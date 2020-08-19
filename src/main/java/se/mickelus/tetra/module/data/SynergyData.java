package se.mickelus.tetra.module.data;

/**
 * Data structure for synergy bonuses. Synergies may either apply based on a set of module variants or a set of modules. This extends
 * ModuleVariantData and all fields inherited that have been set will be applied to the item if the synergy bonus is active.
 */
public class SynergyData extends VariantData {

    // an array of improvements that the item must have for this synergy to apply
    public String[] improvements = new String[0];

    // an array of module variants that the item must have for this synergy to apply
    public String[] moduleVariants = new String[0];

    // an array of modules that the item must have for this synergy to apply
    public String[] modules = new String[0];

    // if set to true then all modules in the modules array also need to be of the same variant,
    // e.g. both pickaxe heads need to be made from iron
    public boolean sameVariant = false;

    // can be used to override the item name for certain module combinations, should be a localization key
    public String name;
}
