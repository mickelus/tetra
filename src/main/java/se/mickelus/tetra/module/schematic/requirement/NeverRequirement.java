package se.mickelus.tetra.module.schematic.requirement;

import se.mickelus.tetra.module.schematic.CraftingContext;

public class NeverRequirement implements CraftingRequirement {
    @Override
    public boolean test(CraftingContext context) {
        return false;
    }
}
