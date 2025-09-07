package se.mickelus.tetra.module.model;

import com.mojang.math.Transformation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import se.mickelus.tetra.items.modular.ItemColors;
import se.mickelus.tetra.module.Priority;

import java.util.Arrays;
import java.util.List;

public class GridTextureModel extends AbstractTextureModel {

    public GridTextureModel(ResourceLocation location) {
        this(location, null, null, null, null, null, Priority.BASE, null, new ItemDisplayContext[]{ItemDisplayContext.NONE});
    }

    public GridTextureModel(ResourceLocation location, ResourceLocation renderType, Transformation transform, Integer emission, Integer tint,
            Integer overlayTint, Priority renderLayer, Boolean invertPerspectives, ItemDisplayContext[] contexts) {
        this.location = location;
        this.renderType = renderType;
        this.transform = transform;
        if (emission != null) {
            this.emission = emission;
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

    public GridTextureModel forMaterial(List<String> availableTextures, String[] modelOverrides, String[] materialTextures, boolean tintOverride,
            int materialTint) {
        if (Arrays.stream(modelOverrides).anyMatch(override -> location.getPath().equals(override))) {
            GridTextureModel copy = copy();
            copy.location = appendString(location, materialTextures[0]);
            copy.tint = tintOverride ? materialTint : 0xffffff;
            copy.overlayTint = materialTint;
            return copy;
        }

        ResourceLocation updatedLocation = Arrays.stream(materialTextures)
                .filter(availableTextures::contains)
                .findFirst()
                .map(texture -> appendString(location, texture))
                .orElseGet(() -> appendString(location, availableTextures.get(0)));
        GridTextureModel copy = copy();
        copy.location = updatedLocation;
        copy.tint = materialTint;
        copy.overlayTint = materialTint;
        return copy;
    }

    protected static ResourceLocation appendString(ResourceLocation resourceLocation, String string) {
        return new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath() + string);
    }

    public GridTextureModel withSlotSuffix(String suffix) {
        GridTextureModel copy = copy();
        copy.location = new ResourceLocation(location.getNamespace(), location.getPath() + suffix);
        return copy;
    }

    public GridTextureModel inheritTint(int parentTint) {
        if (ItemColors.inherit == tint) {
            GridTextureModel copy = copy();
            copy.tint = parentTint;
            return copy;
        }
        return this;
    }

    public GridTextureModel copy() {
        return new GridTextureModel(
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
