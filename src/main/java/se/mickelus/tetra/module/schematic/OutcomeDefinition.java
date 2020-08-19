package se.mickelus.tetra.module.schematic;

import se.mickelus.tetra.module.data.ToolData;

import java.util.HashMap;

/**
 * Used to define outcomes of a schematic, which combination of materials and capabilities yield which variation of a
 * module or improvement.
 * Example json:
 * {
 *     "material": {
 *         "item": "minecraft:oak_planks",
 *         "count": 2
 *     },
 *     "requiredTools": {
 *         "axe": 1,
 *         "hammer": 4
 *     },
 *     "moduleKey": "sword/basic_blade",
 *     "moduleVariant": "basic_blade/acacia",
 *     "improvements": {
 *         "enchantment/looting": 2
 *     }
 * }
 */
public class OutcomeDefinition {

    /**
     * Required material for the outcome. Basically an item predicate, see material documentation for more information.
     * Optional, only required if the schematic which contains this outcome has 1 or more material slots.
     */
    public OutcomeMaterial material = new OutcomeMaterial();

    /**
     * The slot in which to look for the material.
     * Optional, defaults to slot 0;
     */
    public int materialSlot = 0;

    /**
     * The experience cost for the craft.
     */
    public int experienceCost = 0;

    /**
     * An object containing required capabilities and levels for each capability. Available capabilities:
     * hammer, axe, pickaxe, cut, shovel (there may be more, check the Capability class).
     * Optional, if no capabilities are required this field can be omitted.
     *
     * Json format:
     * {
     *     "capabilityA": level,
     *     "capabilityB": level
     * }
     */
    public ToolData requiredTools = new ToolData();

    /**
     * A key referring to a module.
     * Optional, but the schematic should then apply improvements or the outcome with be nothing.
     */
    public String moduleKey;

    /**
     * A module variant, has to be a variant for module specified by the moduleKey field.
     * Optional, but has to be set if the moduleKey field is set.
     */
    public String moduleVariant;

    /**
     * An object describing which improvements to apply to which slot, where the key is the improvement and the value
     * is the improvement level.
     * Optional, this can be used both with and without the moduleKey being set.
     *
     * Json format:
     * {
     *     "improvementA": level,
     *     "improvementB": level
     * }
     */
    public HashMap<String, Integer> improvements = new HashMap<>();
}
