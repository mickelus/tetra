package se.mickelus.tetra.module.model;

import net.minecraft.resources.ResourceLocation;
import se.mickelus.mutil.gui.SimpleColor;
import se.mickelus.tetra.module.Priority;
import se.mickelus.tetra.module.data.MaterialData;

import java.util.List;

public interface IModuleModel {
    public ResourceLocation getType();

    public Priority getRenderLayer();

    public IModuleModel forMaterial(List<String> availableTextures, MaterialData material);

    public IModuleModel withSlotSuffix(String suffix);

    public IModuleModel inheritTint(SimpleColor parentTint);

    public SimpleColor getOverlayTint();
}
