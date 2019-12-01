package se.mickelus.tetra.module.improvement;

import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.module.data.ImprovementData;

import java.util.Arrays;

public class DestabilizationEffect {

    private static DestabilizationEffect[] effects = new DestabilizationEffect[0];

    public String destabilizationKey = "";
    public int minLevel = 0;
    public int maxLevel = 0;

    public float probability = 0;

    public int instabilityLimit = 1;

    public String improvementKey;

    public static void init() {
        DataManager.destabilizationData.onReload(() -> {
            effects = DataManager.destabilizationData.getData().values().stream()
                    .flatMap(Arrays::stream)
                    .toArray(DestabilizationEffect[]::new);
        });
    }

    public static String[] getKeys() {
        return Arrays.stream(effects)
                .map(effect -> effect.destabilizationKey)
                .toArray(String[]::new);
    }

    public static DestabilizationEffect[] getEffectsForImprovement(int instability, ImprovementData[] improvements) {
        return Arrays.stream(effects)
                .filter(effect -> effect.instabilityLimit <= instability)
                .filter(effect -> effect.improvementKey == null
                        || Arrays.stream(improvements).anyMatch(improvement -> improvement.key.equals(effect.improvementKey)))
                .toArray(DestabilizationEffect[]::new);
    }
}
