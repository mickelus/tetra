package se.mickelus.tetra.module.data;

import com.mojang.math.Transformation;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ModuleModel {
    public String type = "item";
    public ResourceLocation location;
    public ResourceLocation renderType;
    public Transformation transform;
    public int emission = 0;
    public int tint = 0xffffffff;
    public int overlayTint = 0xffffffff;

    public ModuleModel() {
    }

    public ModuleModel(ResourceLocation location) {
        this.location = location;
    }

    public ModuleModel(String type, ResourceLocation location) {
        this.type = type;
        this.location = location;
    }

    public ModuleModel copy() {
        ModuleModel copy = new ModuleModel();
        copy.type = type;
        copy.location = location;
        copy.renderType = renderType;
        copy.transform = transform;
        copy.emission = emission;
        copy.tint = tint;
        copy.overlayTint = overlayTint;
        return copy;
    }
}
