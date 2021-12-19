package se.mickelus.tetra.module.data;

import net.minecraft.resources.ResourceLocation;
import se.mickelus.tetra.properties.AttributeHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class MaterialVariantData extends VariantData {

    public ResourceLocation[] materials = {};

    public MaterialMultiplier extract = new MaterialMultiplier();

    public VariantData combine(MaterialData material) {
        UniqueVariantData result = new UniqueVariantData();

        result.key = key + material.key;

        if (material.category != null) {
            result.category = material.category;
        }

        result.attributes = AttributeHelper.collapseRound(AttributeHelper.merge(Arrays.asList(
                attributes,
                AttributeHelper.multiplyModifiers(extract.primaryAttributes, material.primary),
                AttributeHelper.multiplyModifiers(extract.secondaryAttributes, material.secondary),
                AttributeHelper.multiplyModifiers(extract.tertiaryAttributes, material.tertiary)
        )));

        result.durability = Math.round(durability + Optional.ofNullable(extract.durability)
                .map(extracted -> extracted * material.durability)
                .orElse(0f));

        result.durabilityMultiplier = durabilityMultiplier + Optional.ofNullable(extract.durabilityMultiplier)
                .map(extracted -> extracted * material.durability)
                .orElse(0f);

        result.integrity = integrity + Optional.ofNullable(extract.integrity)
                .map(extracted -> extracted * (extracted > 0 ? material.integrityGain : material.integrityCost))
                .map(Math::round)
                .orElse(0);

        result.magicCapacity = Math.round(magicCapacity + Optional.ofNullable(extract.magicCapacity)
                .map(extracted -> extracted * material.magicCapacity)
                .orElse(0f));

        result.effects = EffectData.merge(Arrays.asList(
                effects,
                material.effects,
                EffectData.multiply(extract.primaryEffects, material.primary, material.primary),
                EffectData.multiply(extract.secondaryEffects, material.secondary, material.secondary),
                EffectData.multiply(extract.tertiaryEffects, material.tertiary, material.tertiary)
        ));

        result.tools = ToolData.merge(Arrays.asList(
                tools,
                ToolData.multiply(extract.tools, material.toolLevel, material.toolEfficiency)
        ));

        result.glyph = Optional.ofNullable(extract.glyph)
                .map(glyph -> new GlyphData(glyph.textureLocation, glyph.textureX, glyph.textureY, material.tints.glyph))
                .orElse(glyph);

        result.models = Stream.concat(
                        Arrays.stream(models),
                        Arrays.stream(extract.models).map(model -> new ModuleModel(model.type, combineModelLocation(model.location, material), material.tints.texture)))
                .toArray(ModuleModel[]::new);

        return result;
    }

    private ResourceLocation combineModelLocation(ResourceLocation modelLocation, MaterialData material) {
        if (material.textureOverride) {
            return appendString(modelLocation, material.textures[0]);
        } else {
            List<String> availableTextures = Arrays.asList(extract.availableTextures);
            return Arrays.stream(material.textures)
                    .filter(availableTextures::contains)
                    .findFirst()
                    .map(texture -> appendString(modelLocation, texture))
                    .orElseGet(() -> appendString(modelLocation, availableTextures.get(0)));
        }
    }

    private ResourceLocation appendString(ResourceLocation resourceLocation, String string) {
        return new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath() + string);
    }
}
