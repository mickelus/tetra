package se.mickelus.tetra.module.model;

import com.mojang.math.Transformation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import se.mickelus.mutil.gui.SimpleColor;
import se.mickelus.tetra.module.Priority;

public class FilteredGridTextureModelData extends GridTextureModelData {
    public static final ResourceLocation TYPE = new ResourceLocation("tetra", "filtered_grid_texture");
    private String filter;

    public FilteredGridTextureModelData() {
        super();
    }

    public FilteredGridTextureModelData(ResourceLocation type, ResourceLocation location, ResourceLocation renderType, Transformation transform,
            Integer emission, SimpleColor tint, SimpleColor overlayTint, Priority renderLayer, Boolean invertPerspectives,
            ItemDisplayContext[] contexts, String filter) {
        super(type, location, renderType, transform, emission, tint, overlayTint, renderLayer, invertPerspectives, contexts);
        this.filter = filter;
    }

    public String getFilter() {
        return filter;
    }

    @Override
    public GridTextureModelData copy() {
        return new FilteredGridTextureModelData(type, location, renderType, transform, emission, tint, overlayTint, renderLayer, invertPerspectives, contexts,
                filter);
    }
}
