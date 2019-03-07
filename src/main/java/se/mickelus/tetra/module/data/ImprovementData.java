package se.mickelus.tetra.module.data;

/**
 * Data for improvements are mostly the same as module data but introduces a few additional fields.
 *
 * Example json:
 * {
 *     "key": "enchantment/smite",
 *     "level": 2,
 *     "enchantment": true,
 *     "effects": {
 *         "smite": 2
 *     }
 * }
 */
public class ImprovementData extends ModuleData {

    /**
     * Several improvements with the same key may exist as long as they are of different levels. The level field also
     * matters when converting non-modular items into modular items, enchantments that have a matching improvement with
     * the same level will be added to the modular item.
     * No level label is displayed when the level is set to 0.
     */
    public int level = 0;

    /**
     * Used to tell the modular model renderer to render a texture layer for the improvement.
     */
    public boolean textured = false;

    /**
     * If set to true the item will render with the enchantment glint.
     */
    public boolean enchantment = false;

    public String group = null;

    public int getLevel() {
        return level;
    }
}
