package se.mickelus.tetra;

import java.util.Arrays;

public enum FeatureFlag {
    laminatedStave(true),
    bookEnchantmentSchematic(false),
    bedrockExtraction(false);

    boolean experimental;

    FeatureFlag(boolean experimental) {
        this.experimental = experimental;
    }

    public static boolean isEnabled(FeatureFlag flag) {
        return flag.experimental
                ? ConfigHandler.experimentalFeatures.get().contains(flag.toString())
                : !ConfigHandler.disabledFeatures.get().contains(flag.toString());
    }

    public static boolean matchesAnyKey(Object configKey) {
        return configKey instanceof String && Arrays.stream(values()).anyMatch(flag -> flag.toString().equals(configKey));
    }
}
