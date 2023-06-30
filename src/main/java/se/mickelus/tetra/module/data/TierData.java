package se.mickelus.tetra.module.data;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class TierData<T> {

    public Map<T, Float> levelMap;
    public Map<T, Float> efficiencyMap;

    public TierData() {
        levelMap = new HashMap<>();
        efficiencyMap = new HashMap<>();
    }

    public boolean contains(T key) {
        return levelMap.containsKey(key);
    }

    public int getLevel(T key) {
        if (contains(key)) {
            return Math.round(levelMap.get(key));
        }
        return 0;
    }

    public Map<T, Integer> getLevelMap() {
        return levelMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Math.round(e.getValue())));
    }

    public float getEfficiency(T key) {
        if (efficiencyMap.containsKey(key)) {
            return efficiencyMap.get(key);
        }
        return 0;
    }

    public Set<T> getValues() {
        return Stream.concat(
                        levelMap.entrySet().stream().filter(entry -> entry.getValue() > 0),
                        efficiencyMap.entrySet().stream().filter(entry -> entry.getValue() > 0)
                )
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());

    }
}
