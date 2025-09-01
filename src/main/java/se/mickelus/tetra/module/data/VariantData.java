package se.mickelus.tetra.module.data;

import com.google.common.collect.Multimap;
import com.google.gson.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import se.mickelus.tetra.module.Priority;
import se.mickelus.tetra.module.model.AbstractTextureModel;
import se.mickelus.tetra.properties.AttributeHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class VariantData extends ItemProperties {
    private static final VariantData defaultValues = new VariantData();

    public boolean replace = false;

    /**
     * The key for the module variant. This is used for referencing the module and should be unique. In schematic
     * outcome definitions the moduleVariant field should match this value. Also used as part of localized strings.
     */
    public String key;

    public String category = "misc";

    public Multimap<Attribute, AttributeModifier> attributes;

    /**
     * The tools field is an object describing which tools this item should be usable as, and on which level and with what efficiency it provides that
     * use. The keys in the object should be names of provided tools and the value is an array containing the level and efficiency. The level is an
     * integer and the efficiency is a decimal number. The efficiency is optional and when not provided the level should not be placed within an array.
     * Optional, can be omitted if the module variant provides no tool usages.
     * <p>
     * Json format:
     * {
     * "toolA": [level, efficiency],
     * "toolB": level
     * }
     */
    public ToolData tools;

    /**
     * The effects field is an object describing which effects a module provides, and which level and efficiency that
     * effect has. The keys in the object should be names of provided effects and the value is an array containing the
     * level and efficiency. The level is an integer and the efficiency is a decimal number. The efficiency is optional
     * and when not provided the level should not be placed within an array.
     * Optional, can be omitted if the module variant provides no effects.
     * <p>
     * Json format:
     * {
     * "effectA": [level, efficiency],
     * "effectB": level
     * }
     */
    public EffectData effects;

    public AspectData aspects;

    /**
     * The priority for setting the name of the item. Multiple modules may want to provide an item name e.g. hammer,
     * or pickaxe, and this field is used to prioritize that.
     * Possible values in ascending priority order: LOWEST, LOWER, LOW, BASE, HIGH, HIGHER, HIGHEST
     */
    public Priority namePriority = Priority.BASE;

    /**
     * The priority for setting the prefixes of the item. Multiple modules may want to provide a name prefix e.g.
     * Iron Pickaxe, Tempered steel axe, or Serrated copper shortblade. Only two prefixes are displayed and when more
     * than two are available, this field is used to prioritize which prefixes are used.
     * Possible values in ascending priority order: LOWEST, LOWER, LOW, BASE, HIGH, HIGHER, HIGHEST
     */
    public Priority prefixPriority = Priority.BASE;

    /**
     * The glyph displayed for this module variation, preferrably the same glyph texture is used for all variations of
     * one module while the color differs based on material.
     */
    public GlyphData glyph = new GlyphData();

    public AbstractTextureModel[] models = new AbstractTextureModel[0];

    public int magicCapacity = 0;

    public static VariantData merge(VariantData a, VariantData b) {
        if (b.replace) {
            return b;
        }

        VariantData result = new VariantData();


        result.durability = !Objects.equals(b.durability, defaultValues.durability)
                ? b.durability
                : a.durability;
        result.durabilityMultiplier = !Objects.equals(b.durabilityMultiplier, defaultValues.durabilityMultiplier)
                ? b.durabilityMultiplier
                : a.durabilityMultiplier;
        result.integrity = !Objects.equals(b.integrity, defaultValues.integrity)
                ? b.integrity
                : a.integrity;
        result.integrityMultiplier = !Objects.equals(b.integrityMultiplier, defaultValues.integrityMultiplier)
                ? b.integrityMultiplier
                : a.integrityMultiplier;

        result.key = !Objects.equals(b.key, defaultValues.key)
                ? b.key
                : a.key;

        result.category = !Objects.equals(b.category, defaultValues.category)
                ? b.category
                : a.category;

        result.attributes = AttributeHelper.overwrite(a.attributes, b.attributes);
        result.tools = ToolData.overwrite(a.tools, b.tools);
        result.effects = EffectData.overwrite(a.effects, b.effects);
        result.aspects = AspectData.overwrite(a.aspects, b.aspects);

        result.namePriority = b.namePriority != defaultValues.namePriority
                ? b.namePriority
                : a.namePriority;

        result.prefixPriority = b.prefixPriority != defaultValues.prefixPriority
                ? b.prefixPriority
                : a.prefixPriority;

        result.glyph = !b.glyph.equals(defaultValues.glyph)
                ? b.glyph
                : a.glyph;

        result.models = b.models.length != 0
                ? b.models
                : a.models;

        result.magicCapacity = b.magicCapacity != defaultValues.magicCapacity
                ? b.magicCapacity
                : a.magicCapacity;

        return result;
    }

    public static class Deserializer implements JsonDeserializer<VariantData> {
        @Override
        public VariantData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            if (jsonObject.has("materials")) {
                return context.deserialize(json, MaterialVariantData.class);
            } else {
                return context.deserialize(json, UniqueVariantData.class);
            }
        }
    }
}
