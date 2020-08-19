package se.mickelus.tetra.module.data;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.module.ItemEffect;
import se.mickelus.tetra.properties.AttributeHelper;

import java.util.Map;
import java.util.stream.Collectors;

public class TweakData {
    public String variant;
    public String improvement;

    public String key;
    public int steps;

    private VariantData properties = new VariantData();

    public ItemProperties getProperties(int step) {
        return properties.multiply(step);
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(int step) {
        if (properties.attributes != null) {
            return properties.attributes.entries().stream()
                    .collect(Multimaps.toMultimap(
                            Map.Entry::getKey,
                            entry -> AttributeHelper.multiplyModifier(entry.getValue(), step),
                            ArrayListMultimap::create));
        }

        return null;
    }

    public ToolData getToolData(int step) {
        if (properties.tools != null) {
            ToolData result = new ToolData();
            result.levelMap = properties.tools.levelMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() * step));
            result.efficiencyMap = properties.tools.efficiencyMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() * step));

            return result;
        }

        return null;
    }

    public EffectData getEffectData(int step) {
        if (properties.effects != null) {
            EffectData result = new EffectData();
            result.levelMap = properties.effects.levelMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() * step));
            result.efficiencyMap = properties.effects.efficiencyMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() * step));

            return result;
        }

        return null;
    }


    public int getEffectLevel(ItemEffect effect, int step) {
        return step * properties.effects.getLevel(effect);
    }

    public int getToolLevel(ToolType tool, int step) {
        return step * properties.tools.getLevel(tool);
    }
}
