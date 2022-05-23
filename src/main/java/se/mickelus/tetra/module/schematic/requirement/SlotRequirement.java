package se.mickelus.tetra.module.schematic.requirement;

import se.mickelus.tetra.module.schematic.CraftingContext;

public class SlotRequirement implements CraftingRequirement {
    String slot;

    @Override
    public boolean test(CraftingContext context) {
        return slot.equals(context.slot);
    }
}
