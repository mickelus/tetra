package se.mickelus.tetra.blocks.workbench.unlocks;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import se.mickelus.tetra.blocks.PropertyMatcher;

public class UnlockData {
    public ResourceLocation[] schematics;
    public ResourceLocation[] effects;
    public PropertyMatcher block;
    public AABB bounds = new AABB(-2, 0, -2, 2, 4, 2);
}
