package se.mickelus.tetra.module.data;

import com.google.gson.*;
import net.minecraftforge.common.ToolType;
import org.apache.commons.lang3.EnumUtils;
import se.mickelus.tetra.module.ItemEffect;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
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
                        data.levelMap.put(toolType, entryArray.get(0).getAsInt());
                        data.efficiencyMap.put(toolType, entryArray.get(1).getAsFloat());
                    }
                } else {
                    data.levelMap.put(toolType, entryValue.getAsInt());
                }
            });

            return data;
        }
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
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum));
        result.efficiencyMap = Stream.of(a, b)
                .map(toolData -> toolData.efficiencyMap)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Float::sum));

        return result;
    }

    public static ToolData retainMax(Collection<ToolData> dataCollection) {
        ToolData result = new ToolData();

        dataCollection.forEach(data -> data.getValues().forEach(tool -> {
            int level = data.getLevel(tool);
            if (level > 0 && level >= result.getLevel(tool)) {
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
