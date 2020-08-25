package se.mickelus.tetra.module.data;

import com.google.common.collect.Multimap;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import se.mickelus.tetra.module.schematic.OutcomeMaterial;
import se.mickelus.tetra.properties.AttributeHelper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class MaterialData {
    public boolean replace = false;

    public String key;

    public String category;

    /**
     * Innate attributes gained from the material
     */
    public Multimap<Attribute, AttributeModifier> attributes;

    /**
     * Multiplier for primary attributes, e.g. damage or similar
     */
    public float primary = 1;

    /**
     * Multiplier for secondary attributes, e.g. speed or similar
     */
    public float secondary = 1;

    /**
     * Multiplier for tertiary attributes, not sure which attributes this should map to but I want something more for modpacks/datapacks/compat. Try
     * to keep this balanced so that it can actually be used by modules
     */
    public float tertiary = 1;

    public float durability = 0;

    public float integrityGain = 0;
    public float integrityCost = 0;

    public int magicCapacity = 0;

    /**
     * Innate effects gained from the material
     */
    public EffectData effects = new EffectData();

    /**
     * Multipliers for effects added by modules
     */
    public int effectLevel = 0;
    public float effectEfficiency = 0;

    /**
     * Multipliers for tool levels and efficiency added by modules
     */
    public int toolLevel = 0;
    public float toolEfficiency = 0;

    public MaterialColors tints;

    /**
     * An array of textures that this material would prefer to use, the first one that matches those available for the module will be used. If none
     * of the textures provided here matches one of the available textures for the module then the first available texture for the module will be used.
     */
    public String[] textures = {};

    /**
     * If true all variants of this material will use the first of it's provided textures rather than one from the textures available from the module.
     * Useful where a modded material should use it's own texture rather than one of the defaults, e.g. metals from twilight forest
     */
    public boolean textureOverride = false;

    public OutcomeMaterial material;
    public ToolData requiredTools;

    /**
     * Innate improvements for the material that should be applied if available, e.g. arrested for diamond
     */
    public Map<String, Integer> improvements = new HashMap<>();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Non-configurable stuff below
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static final MaterialData defaultValues = new MaterialData();

    public static void copyFields(MaterialData from, MaterialData to) {
        if (from.key != null) {
            to.key = from.key;
        }

        if (from.category != null) {
            to.category = from.category;
        }

        if (from.primary != defaultValues.primary) {
            to.primary = from.primary;
        }

        if (from.secondary != defaultValues.secondary) {
            to.secondary = from.secondary;
        }

        if (from.tertiary != defaultValues.tertiary) {
            to.tertiary = from.tertiary;
        }

        if (from.durability != defaultValues.durability) {
            to.durability = from.durability;
        }

        if (from.integrityGain != defaultValues.integrityGain) {
            to.integrityGain = from.integrityGain;
        }

        if (from.integrityCost != defaultValues.integrityCost) {
            to.integrityCost = from.integrityCost;
        }

        if (from.magicCapacity != defaultValues.magicCapacity) {
            to.magicCapacity = from.magicCapacity;
        }


        if (from.effectLevel != defaultValues.effectLevel) {
            to.effectLevel = from.effectLevel;
        }

        if (from.effectEfficiency != defaultValues.effectEfficiency) {
            to.effectEfficiency = from.effectEfficiency;
        }

        if (from.toolLevel != defaultValues.toolLevel) {
            to.toolLevel = from.toolLevel;
        }

        if (from.toolEfficiency != defaultValues.toolEfficiency) {
            to.toolEfficiency = from.toolEfficiency;
        }

        if (from.tints != null) {
            to.tints = from.tints;
        }

        if (from.textureOverride != defaultValues.textureOverride) {
            to.textureOverride = from.textureOverride;
        }

        to.attributes = AttributeHelper.overwrite(to.attributes, from.attributes);
        to.effects = EffectData.overwrite(to.effects, from.effects);
        to.requiredTools = ToolData.overwrite(to.requiredTools, from.requiredTools);

        if (from.material != null) {
            to.material = from.material;
        }

        to.textures = Stream.concat(Arrays.stream(to.textures), Arrays.stream(from.textures))
                .distinct()
                .toArray(String[]::new);

        if (from.improvements != null) {
            if (to.improvements != null) {
                Map<String, Integer> merged = new HashMap<>();
                merged.putAll(to.improvements);
                merged.putAll(from.improvements);
                to.improvements = merged;
            } else {
                to.improvements = from.improvements;
            }
        }
    }

    public MaterialData shallowCopy() {
        MaterialData copy = new MaterialData();
        copyFields(this, copy);

        return copy;
    }
}
