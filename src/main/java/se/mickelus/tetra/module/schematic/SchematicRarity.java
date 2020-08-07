package se.mickelus.tetra.module.schematic;

/**
 * The rarity of a schematic affects how it is rendered. Colors and effects are used to differentiate between effects.
 */
public enum SchematicRarity {

    temporary(0xffdfaa),
    hone(0xceceff),
    basic(0xffffff);

    public int tint;

    SchematicRarity(int tint) {
        this.tint = tint;
    }
}
