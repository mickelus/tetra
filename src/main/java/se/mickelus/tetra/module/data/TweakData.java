package se.mickelus.tetra.module.data;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.properties.AttributeHelper;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TweakData {
    private final VariantData properties = new VariantData();
    public String variant;
    public String improvement;
    public String key;
    public int steps;

    public ItemProperties getProperties(int step) {
        return properties.multiply(step);
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(int step) {
        return AttributeHelper.multiplyModifiers(properties.attributes, step);
    }

    public ToolData getToolData(int step) {
        return ToolData.multiply(properties.tools, step, step);
    }

    public EffectData getEffectData(int step) {
        return EffectData.multiply(properties.effects, step, step);
    }


    public int getEffectLevel(ItemEffect effect, int step) {
        return step * properties.effects.getLevel(effect);
    }

    public int getToolLevel(ToolAction tool, int step) {
        return step * properties.tools.getLevel(tool);
    }
}
