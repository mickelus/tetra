package se.mickelus.tetra.module.model;

import com.mojang.math.Transformation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import se.mickelus.mutil.gui.SimpleColor;
import se.mickelus.tetra.items.modular.ItemColors;
import se.mickelus.tetra.module.Priority;

import java.util.Arrays;
import java.util.List;

public class GridTextureModelData extends AbstractTextureModelData {
    public static final ResourceLocation TYPE = new ResourceLocation("tetra", "grid_texture");

    public GridTextureModelData() {
        super();
    }

    public GridTextureModelData(ResourceLocation location) {
        this(TYPE, location, null, null, 0, null, null, null, false, null);
    }

    public GridTextureModelData(ResourceLocation type, ResourceLocation location, ResourceLocation renderType, Transformation transform,
            Integer emission, SimpleColor tint, SimpleColor overlayTint, Priority renderLayer, Boolean invertPerspectives,
            ItemDisplayContext[] contexts) {
        super();
        this.type = type;
        this.location = location;
        this.renderType = renderType;
        this.transform = transform;
        if (emission != null) {
            this.emission = Mth.clamp(0, emission, 15);
        }
        if (tint != null) {
            this.tint = tint;
        }
        if (overlayTint != null) {
            this.overlayTint = overlayTint;
        }
        if (renderLayer != null) {
            this.renderLayer = renderLayer;
        }
        if (invertPerspectives != null) {
            this.invertPerspectives = invertPerspectives;
        }
        this.contexts = contexts;
    }

    public GridTextureModelData forMaterial(List<String> availableTextures, String[] modelOverrides, String[] materialTextures, boolean tintOverride,
            SimpleColor materialTint) {
        if (Arrays.stream(modelOverrides).anyMatch(override -> location.getPath().equals(override))) {
            GridTextureModelData copy = copy();
            copy.location = appendString(location, materialTextures[0]);
            copy.tint = tintOverride ? materialTint : new SimpleColor(0xffffffff);
            copy.overlayTint = materialTint;
            return copy;
        }

        ResourceLocation updatedLocation = Arrays.stream(materialTextures)
                .filter(availableTextures::contains)
                .findFirst()
                .map(texture -> appendString(location, texture))
                .orElseGet(() -> appendString(location, availableTextures.get(0)));
        GridTextureModelData copy = copy();
        copy.location = updatedLocation;
        copy.tint = materialTint;
        copy.overlayTint = materialTint;
        return copy;
    }

    protected static ResourceLocation appendString(ResourceLocation resourceLocation, String string) {
        return new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath() + string);
    }

    public GridTextureModelData withSlotSuffix(String suffix) {
        GridTextureModelData copy = copy();
        copy.location = new ResourceLocation(location.getNamespace(), location.getPath() + suffix);
        return copy;
    }

    public GridTextureModelData inheritTint(SimpleColor parentTint) {
        if (ItemColors.inherit == tint.getRaw()) {
            GridTextureModelData copy = copy();
            copy.tint = parentTint;
            return copy;
        }
        return this;
    }

    public GridTextureModelData copy() {
        return new GridTextureModelData(
                type,
                location,
                renderType,
                transform,
                emission,
                tint,
                overlayTint,
                renderLayer,
                invertPerspectives,
                contexts);
    }
}
