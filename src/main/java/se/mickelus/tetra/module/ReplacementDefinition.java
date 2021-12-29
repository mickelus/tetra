package se.mickelus.tetra.module;

import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.item.ItemStack;

/**
 * Used when converting vanilla or non-tetra items into modular items, the fields in the class differs a fair amount
 * from the json format.
 *
 * Example json:
 * {
 *     "predicate": { "item": "minecraft:wooden_axe" },
 *     "item": "tetra:modular_double",
 *     "modules": {
 *         "double/head_left": [ "double/basic_axe_left", "basic_axe/oak" ],
 *         "double/head_right": [ "double/butt_right", "butt/oak" ],
 *         "double/handle": [ "double/basic_handle", "basic_handle/stick" ]
 *     },
 *     "improvements": {
 *         "double/head_left:hone/efficiency": 1
 *     }
 * }
 */
public class ReplacementDefinition {

    /**
     * An item predicate used to match against the input item of the replacement. Parsed as a regular item predicate
     * (see the vanilla wiki for more details on how they work), can be of other item predicate types such as the
     * oredict item predicate.
     *
     * Example json:
     * { "item": "minecraft:diamond_hoe" }
     */
    public ItemPredicate predicate;

    /**
     * The itemstack is generated from the item, modules & improvements json fields, every time a non-modular item
     * is used in a modular manner this itemstack is cloned and used instead of the non-modular item.
     *
     * "item" is a resourcelocation, preferrably refering to a tetra modular item.
     * Json format: "domain:path"
     *
     * The "modules" field defines the modules of the replacing modular item. It is a json object where the keys
     * represent a slot and the value should be an array containing two strings, where the first is the name of a module
     * and the second is a variant of that module. All required slots for the item (both minor and major modules)
     * should be specified in this object, other slots are optional.
     * Json format:
     * {
     *     "slot1": [ "moduleA", "variantX" ],
     *     "slot2": [ "moduleB", "variantY" ]
     * }
     *
     * The "improvements" field defines improvements for major modules. It is a json object where keys are a combination
     * of a slot and an improvement and the value is the level of the improvement.
     * Json format:
     * {
     *     "slot1:improvementA": improvementALevel,
     *     "slot2:improvementB": improvementBLevel
     * }
     */
    public ItemStack itemStack;
}
