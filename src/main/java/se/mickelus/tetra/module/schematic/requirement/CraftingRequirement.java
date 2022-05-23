package se.mickelus.tetra.module.schematic.requirement;

import se.mickelus.tetra.module.schematic.CraftingContext;

public interface CraftingRequirement {
    public static final CraftingRequirement any = (ctx) -> true;

    public boolean test(CraftingContext context);
}
