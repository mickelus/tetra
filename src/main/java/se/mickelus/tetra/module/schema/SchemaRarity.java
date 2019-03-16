package se.mickelus.tetra.module.schema;

/**
 * The rarity of a schema affects how it is rendered. Colors and effects are used to differentiate between effects.
 */
public enum SchemaRarity {

    temporary(0xffdfaa),
    basic(0xffffff);

    public int tint;

    SchemaRarity(int tint) {
        this.tint = tint;
    }
}
