package se.mickelus.tetra.module.data;

import com.google.gson.*;
import org.apache.commons.lang3.EnumUtils;
import se.mickelus.tetra.module.ItemEffect;

import java.lang.reflect.Type;

public class EffectData extends EnumTierData<ItemEffect> {

    // todo: is this possible to implement as a generic?
    public static class Deserializer implements JsonDeserializer<EffectData> {

        @Override
        public EffectData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            EffectData data = new EffectData();

            jsonObject.entrySet().stream()
                    .filter(entry -> EnumUtils.isValidEnum(ItemEffect.class, entry.getKey()))
                    .forEach(entry -> {
                        JsonElement entryValue = entry.getValue();
                        if (entryValue.isJsonArray()) {
                            JsonArray entryArray = entryValue.getAsJsonArray();
                            if (entryArray.size() == 2) {
                                data.valueMap.put(ItemEffect.valueOf(entry.getKey()), entryArray.get(0).getAsInt());
                                data.efficiencyMap.put(ItemEffect.valueOf(entry.getKey()), entryArray.get(1).getAsFloat());
                            }
                        } else {
                            data.valueMap.put(ItemEffect.valueOf(entry.getKey()), entryValue.getAsInt());
                        }
                    });

            return data;
        }
    }
}
