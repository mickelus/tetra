package se.mickelus.tetra.module.data;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
@ParametersAreNonnullByDefault
public class EnumTierData<T extends Enum> {

    public Map<T, Integer> levelMap;
    public Map<T, Float> efficiencyMap;

    public EnumTierData() {
        levelMap = new HashMap<>();
        efficiencyMap = new HashMap<>();
    }

    public boolean contains(T key) {
        return levelMap.containsKey(key);
    }

    public int getLevel(T key) {
        if (contains(key)) {
            return levelMap.get(key);
        }
        return 0;
    }

    public float getEfficiency(T key) {
        if (efficiencyMap.containsKey(key)) {
            return efficiencyMap.get(key);
        }
        return 0;
    }

    public Set<T> getValues() {
        return levelMap.keySet();
    }
}
