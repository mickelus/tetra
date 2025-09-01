package se.mickelus.tetra.module.model;

import se.mickelus.tetra.module.Priority;

import java.util.List;

public interface IModuleModel {
    public Priority getRenderLayer();

    public IModuleModel forMaterial(List<String> availableTextures, String[] modelOverrides, String[] materialTextures, boolean tintOverride,
            int materialTint);

    public IModuleModel withSlotSuffix(String suffix);

    public IModuleModel inheritTint(int parentTint);
}
