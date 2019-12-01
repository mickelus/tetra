package se.mickelus.tetra.module.data;

import net.minecraft.util.ResourceLocation;

public class ModuleModel {
    public String type = "item";
    public ResourceLocation location;
    public int tint = 0xffffffff;

    public ModuleModel() {}

    public ModuleModel(ResourceLocation location) {
        this.location = location;
    }
}
