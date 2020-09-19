package se.mickelus.tetra.module.schematic;

import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.module.data.*;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MaterialOutcomeDefinition extends OutcomeDefinition {

    public ResourceLocation[] materials = {};

    public int countOffset = 0;
    public float countFactor = 1;

    public int toolOffset = 0;
    public float toolFactor = 1;


    public OutcomeDefinition combine(MaterialData materialData) {
        UniqueOutcomeDefinition result = new UniqueOutcomeDefinition();

        result.moduleKey = moduleKey;

        if (moduleVariant != null) {
            result.moduleVariant = moduleVariant + materialData.key;
        }

        result.material = materialData.material.offsetCount(countFactor, countOffset);

        if (materialData.requiredTools != null) {
            result.requiredTools = ToolData.offsetLevel(materialData.requiredTools, toolFactor, toolOffset);
        }

        result.requiredTools = ToolData.merge(requiredTools,
                Optional.ofNullable(materialData.requiredTools)
                        .map(materialTools -> ToolData.offsetLevel(materialTools, toolFactor, toolOffset))
                        .orElse(null));

        result.improvements = Stream.of(improvements, materialData.improvements)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::max));

        return result;
    }
}
