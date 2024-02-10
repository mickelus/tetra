package se.mickelus.tetra.module.schematic.requirement;

import se.mickelus.tetra.FeatureFlag;
import se.mickelus.tetra.module.schematic.CraftingContext;

public class FeatureFlagRequirement implements CraftingRequirement {
    FeatureFlag feature;

    @Override
    public boolean test(CraftingContext context) {
        return FeatureFlag.isEnabled(feature);
    }
}
