package se.mickelus.tetra.module.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EnumTierData<T extends Enum> {

    public Map<T, Integer> valueMap;
    public Map<T, Float> efficiencyMap;

    public EnumTierData() {
        valueMap = new HashMap<>();
        efficiencyMap = new HashMap<>();
    }

    public boolean contains(T capability) {
        return valueMap.containsKey(capability);
    }

    public int getLevel(T capability) {
        if (contains(capability)) {
            return valueMap.get(capability);
        }
        return 0;
    }

    public float getEfficiency(T capability) {
        if (efficiencyMap.containsKey(capability)) {
            return efficiencyMap.get(capability);
        }
        return 0;
    }

    public Set<T> getValues() {
        return valueMap.keySet();
    }
}
