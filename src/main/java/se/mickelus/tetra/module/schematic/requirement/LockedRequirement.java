package se.mickelus.tetra.module.schematic.requirement;

import net.minecraft.resources.ResourceLocation;
import se.mickelus.tetra.module.schematic.CraftingContext;

import java.util.Arrays;

public class LockedRequirement implements CraftingRequirement {
    public ResourceLocation key;
    @Override
    public boolean test(CraftingContext context) {
        return Arrays.asList(context.unlocks).contains(key);
    }
}
