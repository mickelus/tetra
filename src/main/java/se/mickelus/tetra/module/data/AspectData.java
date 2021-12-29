package se.mickelus.tetra.module.data;

import com.google.gson.*;
import se.mickelus.tetra.aspect.ItemAspect;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AspectData extends TierData<ItemAspect> {

    public static AspectData overwrite(AspectData a, AspectData b) {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        }

        AspectData result = new AspectData();
        result.levelMap.putAll(a.levelMap);
        result.efficiencyMap.putAll(a.efficiencyMap);
        b.levelMap.forEach(result.levelMap::put);
        b.efficiencyMap.forEach(result.efficiencyMap::put);

        return result;
    }

    public static AspectData merge(Collection<AspectData> data) {
        return data.stream().reduce(null, AspectData::merge);
    }

    public static AspectData merge(AspectData a, AspectData b) {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        }

        AspectData result = new AspectData();
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

    public static AspectData multiply(AspectData aspectData, float levelMultiplier, float efficiencyMultiplier) {
        return Optional.ofNullable(aspectData)
                .map(data -> {
                    AspectData result = new AspectData();
                    result.levelMap = data.levelMap.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() * levelMultiplier));
                    result.efficiencyMap = data.efficiencyMap.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() * efficiencyMultiplier));
                    return result;
                })
                .orElse(null);
    }

    public static class Deserializer implements JsonDeserializer<AspectData> {

        @Override
        public AspectData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            AspectData data = new AspectData();

            jsonObject.entrySet().forEach(entry -> {
                JsonElement entryValue = entry.getValue();
                ItemAspect aspect = ItemAspect.get(entry.getKey());
                if (entryValue.isJsonArray()) {
                    JsonArray entryArray = entryValue.getAsJsonArray();
                    if (entryArray.size() == 2) {
                        data.levelMap.put(aspect, entryArray.get(0).getAsFloat());
                        data.efficiencyMap.put(aspect, entryArray.get(1).getAsFloat());
                    }
                } else {
                    data.levelMap.put(aspect, entryValue.getAsFloat());
                }
            });

            return data;
        }
    }
}
