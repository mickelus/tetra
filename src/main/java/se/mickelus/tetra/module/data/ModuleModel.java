package se.mickelus.tetra.module.data;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ModuleModel {
    public String type = "item";
    public ResourceLocation location;
    public int tint = 0xffffffff;

    public ModuleModel() {
    }

    public ModuleModel(ResourceLocation location) {
        this.location = location;
    }

    public ModuleModel(String type, ResourceLocation location) {
        this.type = type;
        this.location = location;
    }

    public ModuleModel(String type, ResourceLocation location, int tint) {
        this.type = type;
        this.location = location;
        this.tint = tint;
    }
}
