package se.mickelus.tetra.module.schematic.requirement;

import se.mickelus.tetra.module.schematic.CraftingContext;

import java.util.Arrays;

public class AndRequirement implements CraftingRequirement {

    CraftingRequirement[] requirements;

    @Override
    public boolean test(CraftingContext context) {
        return Arrays.stream(requirements).allMatch(requirement -> requirement.test(context));
    }
}
