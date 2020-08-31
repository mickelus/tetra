package se.mickelus.tetra.module.data;

import com.google.gson.*;
import se.mickelus.tetra.module.ItemEffect;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EffectData extends TierData<ItemEffect> {

    public static EffectData overwrite(EffectData a, EffectData b) {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        }

        EffectData result = new EffectData();
        result.levelMap.putAll(a.levelMap);
        result.efficiencyMap.putAll(a.efficiencyMap);
        b.levelMap.forEach(result.levelMap::put);
        b.efficiencyMap.forEach(result.efficiencyMap::put);

        return result;
    }

    public static EffectData merge(Collection<EffectData> data) {
        return data.stream().reduce(null, EffectData::merge);
    }

    public static EffectData merge(EffectData a, EffectData b) {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        }

        EffectData result = new EffectData();
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

    public static EffectData multiply(EffectData effectData, float levelMultiplier, float efficiencyMultiplier) {
        return Optional.ofNullable(effectData)
                .map(data -> {
                    EffectData result = new EffectData();
                    result.levelMap = data.levelMap.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() * levelMultiplier));
                    result.efficiencyMap = data.efficiencyMap.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() * efficiencyMultiplier));
                    return result;
                })
                .orElse(null);
    }

    // todo: is this possible to implement as a generic?
    public static class Deserializer implements JsonDeserializer<EffectData> {

        @Override
        public EffectData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            EffectData data = new EffectData();

            jsonObject.entrySet().forEach(entry -> {
                JsonElement entryValue = entry.getValue();
                ItemEffect effect = ItemEffect.get(entry.getKey());
                if (entryValue.isJsonArray()) {
                    JsonArray entryArray = entryValue.getAsJsonArray();
                    if (entryArray.size() == 2) {
                        data.levelMap.put(effect, entryArray.get(0).getAsFloat());
                        data.efficiencyMap.put(effect, entryArray.get(1).getAsFloat());
                    }
                } else {
                    data.levelMap.put(effect, entryValue.getAsFloat());
                }
            });

            return data;
        }
    }
}
