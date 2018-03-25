package se.mickelus.tetra.module;

import com.google.gson.*;
import org.apache.commons.lang3.EnumUtils;
import se.mickelus.tetra.capabilities.Capability;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CapabilityData {

    public Map<Capability, Integer> capabilityMap;
    public Map<Capability, Float> efficiencyMap;

    public CapabilityData() {
        capabilityMap = new HashMap<>();
        efficiencyMap = new HashMap<>();
    }

    public boolean containsCapability(Capability capability) {
        return capabilityMap.containsKey(capability);
    }

    public int getCapabilityLevel(Capability capability) {
        if (containsCapability(capability)) {
            return capabilityMap.get(capability);
        }
        return 0;
    }

    public float getCapabilityEfficiency(Capability capability) {
        if (efficiencyMap.containsKey(capability)) {
            return efficiencyMap.get(capability);
        }
        return 1;
    }

    public Set<Capability> getCapabilities() {
        return capabilityMap.keySet();
    }

    public static class CapabilityTypeAdapter implements JsonDeserializer<CapabilityData> {
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
                                data.capabilityMap.put(Capability.valueOf(entry.getKey()), entryArray.get(0).getAsInt());
                                data.efficiencyMap.put(Capability.valueOf(entry.getKey()), entryArray.get(1).getAsFloat());
                            }
                        } else {
                            data.capabilityMap.put(Capability.valueOf(entry.getKey()), entryValue.getAsInt());
                        }
                    });

            return data;
        }
    }
}
