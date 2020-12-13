package se.mickelus.tetra.module.schematic;

import se.mickelus.tetra.gui.GuiColors;

/**
 * The rarity of a schematic affects how it is rendered. Colors and effects are used to differentiate between effects.
 */
public enum SchematicRarity {

    temporary(GuiColors.temporarySchematic),
    hone(GuiColors.hone),
    basic(GuiColors.normal);

    public int tint;

    SchematicRarity(int tint) {
        this.tint = tint;
    }
}
