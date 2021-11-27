package se.mickelus.tetra.module.schematic;

/**
 * Defines how a schematic should look, primarily affects the outline around the schematic glyph.
 */
public enum SchematicType {
    /**
     * Renders no outline.
     */
    other,

    /**
     * Renders in the same way as major, but with a + int he bottom left. Use for improvement schematics.
     */
    improvement,

    /**
     * Renders the same outline as shown around minor modules, use for minor module schematics.
     */
    minor,

    /**
     * Renders a similar outline as major modules, but cut off at the top and the bottom. Use for major module schematics.
     */
    major
}
