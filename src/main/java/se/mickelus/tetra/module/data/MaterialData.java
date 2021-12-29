package se.mickelus.tetra.module.data;

import com.google.common.collect.Multimap;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.module.schematic.OutcomeMaterial;
import se.mickelus.tetra.properties.AttributeHelper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MaterialData {
    public boolean replace = false;

    public String key;

    public String category = "misc";

    /**
     * If set to true, material will not show up in the holosphere.
     */
    public boolean hidden = false;

    /**
     * If set to true, module and improvement variants derived from this material will not show up in the holosphere.
     */
    public boolean hiddenOutcomes = false;

    /**
     * Innate attributes gained from the material
     */
    public Multimap<Attribute, AttributeModifier> attributes;

    /**
     * Multiplier for primary attributes, e.g. damage or similar
     */
    public Float primary;

    /**
     * Multiplier for secondary attributes, e.g. speed or similar
     */
    public Float secondary;

    /**
     * Multiplier for tertiary attributes, not sure which attributes this should map to but I want something more for modpacks/datapacks/compat. Try
     * to keep this balanced so that it can actually be used by modules
     */
    public Float tertiary;

    public float durability = 0;

    public float integrityGain = 0;
    public float integrityCost = 0;

    public int magicCapacity = 0;

    /**
     * Innate effects gained from the material
     */
    public EffectData effects = new EffectData();

    public AspectData aspects = new AspectData();

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

    public String[] textureOverrides = {};
    public boolean tintOverrides = false;

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

        if (from.hidden != defaultValues.hidden) {
            to.hidden = from.hidden;
        }

        if (from.hiddenOutcomes != defaultValues.hiddenOutcomes) {
            to.hiddenOutcomes = from.hiddenOutcomes;
        }

        if (from.category != null) {
            to.category = from.category;
        }

        if (from.primary != null) {
            to.primary = from.primary;
        }

        if (from.secondary != null) {
            to.secondary = from.secondary;
        }

        if (from.tertiary != null) {
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

        if (from.toolLevel != defaultValues.toolLevel) {
            to.toolLevel = from.toolLevel;
        }

        if (from.toolEfficiency != defaultValues.toolEfficiency) {
            to.toolEfficiency = from.toolEfficiency;
        }

        if (from.tints != null) {
            to.tints = from.tints;
        }

        to.textureOverrides = Stream.concat(Arrays.stream(to.textureOverrides), Arrays.stream(from.textureOverrides))
                .distinct()
                .toArray(String[]::new);

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


    public static ModuleModel kneadModel(ModuleModel model, MaterialData material, List<String> availableTextures) {
        if (Arrays.stream(material.textureOverrides).anyMatch(override -> model.location.getPath().equals(override))) {
            return new ModuleModel(model.type, appendString(model.location, material.textures[0]),
                    material.tintOverrides ? material.tints.texture : 0xffffff, material.tints.texture);
        }

        ResourceLocation updatedLocation = Arrays.stream(material.textures)
                .filter(availableTextures::contains)
                .findFirst()
                .map(texture -> appendString(model.location, texture))
                .orElseGet(() -> appendString(model.location, availableTextures.get(0)));

        return new ModuleModel(model.type, updatedLocation, material.tints.texture);
    }

    public static ResourceLocation appendString(ResourceLocation resourceLocation, String string) {
        return new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath() + string);
    }
}
