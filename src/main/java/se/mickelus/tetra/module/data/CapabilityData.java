package se.mickelus.tetra.module.data;

import com.google.gson.*;
import org.apache.commons.lang3.EnumUtils;
import se.mickelus.tetra.capabilities.Capability;

import java.lang.reflect.Type;

public class CapabilityData extends EnumTierData<Capability> {

    // todo: is this possible to implement as a generic?
    public static class Deserializer implements JsonDeserializer<CapabilityData> {

        @Override
        public CapabilityData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            CapabilityData data = new CapabilityData();

            jsonObject.entrySet().stream()
                    .filter(entry -> EnumUtils.isValidEnum(Capability.class, entry.getKey()))
                    .forEach(entry -> {
                        JsonElement entryValue = entry.getValue();
                        if (entryValue.isJsonArray()) {
                            JsonArray entryArray = entryValue.getAsJsonArray();
                            if (entryArray.size() == 2) {
                                data.valueMap.put(Capability.valueOf(entry.getKey()), entryArray.get(0).getAsInt());
                                data.efficiencyMap.put(Capability.valueOf(entry.getKey()), entryArray.get(1).getAsFloat());
                            }
                        } else {
                            data.valueMap.put(Capability.valueOf(entry.getKey()), entryValue.getAsInt());
                        }
                    });

            return data;
        }
    }
}
