package se.mickelus.tetra.module.schematic.requirement;

import se.mickelus.tetra.module.schematic.CraftingContext;

public class NotRequirement implements CraftingRequirement {

    CraftingRequirement requirement;

    @Override
    public boolean test(CraftingContext context) {
        return !requirement.test(context);
    }
}
