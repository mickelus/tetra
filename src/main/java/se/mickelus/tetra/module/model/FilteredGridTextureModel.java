package se.mickelus.tetra.module.model;

import com.mojang.math.Transformation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import se.mickelus.tetra.module.Priority;

public class FilteredGridTextureModel extends GridTextureModel {
    private String filter;

    public FilteredGridTextureModel(ResourceLocation location, ResourceLocation renderType, Transformation transform, Integer emission, Integer tint,
            Integer overlayTint, Priority renderLayer, Boolean invertPerspectives, ItemDisplayContext[] contexts, String filter) {
        super(location, renderType, transform, emission, tint, overlayTint, renderLayer, invertPerspectives, contexts);
        this.filter = filter;
    }

    public String getFilter() {
        return filter;
    }
}
