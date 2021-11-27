package se.mickelus.tetra.module.data;

import com.google.gson.*;
import net.minecraftforge.common.ToolType;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ToolData extends TierData<ToolType> {
    public static class Deserializer implements JsonDeserializer<ToolData> {
        @Override
        public ToolData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            ToolData data = new ToolData();

            jsonObject.entrySet().forEach(entry -> {
                JsonElement entryValue = entry.getValue();
                ToolType toolType = ToolType.get(entry.getKey());
                if (entryValue.isJsonArray()) {
                    JsonArray entryArray = entryValue.getAsJsonArray();
                    if (entryArray.size() == 2) {
                        data.levelMap.put(toolType, entryArray.get(0).getAsFloat());
                        data.efficiencyMap.put(toolType, entryArray.get(1).getAsFloat());
                    }
                } else {
                    data.levelMap.put(toolType, entryValue.getAsFloat());
                }
            });

            return data;
        }
    }

    public static ToolData overwrite(ToolData a, ToolData b) {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        }

        ToolData result = new ToolData();
        result.levelMap.putAll(a.levelMap);
        result.efficiencyMap.putAll(a.efficiencyMap);
        b.levelMap.forEach(result.levelMap::put);
        b.efficiencyMap.forEach(result.efficiencyMap::put);

        return result;
    }

    public static ToolData merge(Collection<ToolData> data) {
        return data.stream().reduce(null, ToolData::merge);
    }

    public static ToolData merge(ToolData a, ToolData b) {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        }

        ToolData result = new ToolData();
        result.levelMap = Stream.of(a, b)
                .map(toolData -> toolData.levelMap)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Float::sum));
        result.efficiencyMap = Stream.of(a, b)
                .map(toolData -> toolData.efficiencyMap)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Float::sum));

        return result;
    }

    public static ToolData multiply(ToolData toolData, float levelMultiplier, float efficiencyMultiplier) {
        return Optional.ofNullable(toolData)
                .map(data -> {
                    ToolData result = new ToolData();
                    result.levelMap = data.levelMap.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() * levelMultiplier));
                    result.efficiencyMap = data.efficiencyMap.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() * efficiencyMultiplier));
                    return result;
                })
                .orElse(null);
    }

    public static ToolData offsetLevel(ToolData toolData, float multiplier, int offset) {
        return Optional.ofNullable(toolData)
                .map(data -> {
                    ToolData result = new ToolData();
                    result.levelMap = data.levelMap.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() * multiplier + offset));
                    result.efficiencyMap = data.efficiencyMap;
                    return result;
                })
                .orElse(null);
    }

    public static ToolData retainMax(Collection<ToolData> dataCollection) {
        ToolData result = new ToolData();

        dataCollection.forEach(data -> data.getValues().forEach(tool -> {
            float level = data.levelMap.getOrDefault(tool, 0f);
            if (level >= result.levelMap.getOrDefault(tool, 0f)) {
                result.levelMap.put(tool, level);
                if (data.getEfficiency(tool) > result.getEfficiency(tool)) {
                    result.efficiencyMap.put(tool, data.getEfficiency(tool));
                }
            }
        }));

        // used when this was used for all modules, should no longer be needed
//        datas.forEach(data -> data.getValues().forEach(tool -> {
//            float efficiency = data.getEfficiency(tool);
//            if (data.getLevel(tool) == 0 && efficiency > 0) {
//                result.efficiencyMap.put(tool, result.getEfficiency(tool) + efficiency);
//            }
//        }));

        return result;
    }
}
