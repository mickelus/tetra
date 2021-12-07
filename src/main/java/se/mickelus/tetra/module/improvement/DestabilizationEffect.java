package se.mickelus.tetra.module.improvement;

import com.google.gson.JsonObject;
import net.minecraftforge.fml.ModList;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.module.data.ImprovementData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
@ParametersAreNonnullByDefault
public class DestabilizationEffect {

    private static DestabilizationEffect[] effects = new DestabilizationEffect[0];

    public String destabilizationKey = "";
    public int minLevel = 0;
    public int maxLevel = 0;

    public String requiredMod;

    public float probability = 0;

    public int instabilityLimit = 1;

    public String improvementKey;

    public static void init() {
        DataManager.instance.destabilizationData.onReload(() -> {
            effects = DataManager.instance.destabilizationData.getData().values().stream()
                    .flatMap(Arrays::stream)
                    .filter(effect -> effect.requiredMod == null || ModList.get().isLoaded(effect.requiredMod))
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

    public JsonObject toJson() {
        JsonObject result = new JsonObject();

        result.addProperty("destabilizationKey", destabilizationKey);

        if (minLevel != 0) {
            result.addProperty("minLevel", minLevel);
        }

        if (minLevel != 0) {
            result.addProperty("minLevel", minLevel);
        }

        if (maxLevel != 0) {
            result.addProperty("maxLevel", maxLevel);
        }

        if (requiredMod != null) {
            result.addProperty("requiredMod", requiredMod);
        }

        if (probability != 0) {
            result.addProperty("probability", probability);
        }

        if (instabilityLimit != 1) {
            result.addProperty("instabilityLimit", instabilityLimit);
        }

        if (improvementKey != null) {
            result.addProperty("improvementKey", improvementKey);
        }

        return result;
    }
}
