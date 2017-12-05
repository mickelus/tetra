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

    public CapabilityData() {
        capabilityMap = new HashMap<>();
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
                    .forEach(entry -> data.capabilityMap.put(Capability.valueOf(entry.getKey()), entry.getValue().getAsInt()));

            return data;
        }
    }
}
