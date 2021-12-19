package se.mickelus.tetra.module.data;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class MaterialMultiplier {
    public Multimap<Attribute, AttributeModifier> primaryAttributes;
    public Multimap<Attribute, AttributeModifier> secondaryAttributes;
    public Multimap<Attribute, AttributeModifier> tertiaryAttributes;

    public Float durability;
    public Float durabilityMultiplier;

    public Float integrity;
    public Float magicCapacity;

    public EffectData primaryEffects;
    public EffectData secondaryEffects;
    public EffectData tertiaryEffects;
    public ToolData tools;

    public GlyphData glyph;

    public String[] availableTextures = {};
    public ModuleModel[] models = {};
}
