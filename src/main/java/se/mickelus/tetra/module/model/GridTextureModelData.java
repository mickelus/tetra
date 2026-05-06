package se.mickelus.tetra.module.model;

import com.mojang.math.Transformation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import se.mickelus.mutil.gui.SimpleColor;
import se.mickelus.tetra.items.modular.ItemColors;
import se.mickelus.tetra.module.Priority;
import se.mickelus.tetra.module.data.MaterialData;

import java.util.Arrays;
import java.util.List;

public class GridTextureModelData extends AbstractTextureModelData {
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath("tetra", "grid_texture");

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

    public GridTextureModelData forMaterial(List<String> availableTextures, MaterialData material) {
        if (Arrays.stream(material.textureOverrides).anyMatch(override -> location.getPath().equals(override))) {
            GridTextureModelData copy = copy();
            copy.location = appendString(location, material.textures[0]);
            copy.tint = material.tintOverrides ? new SimpleColor(material.tints.texture) : new SimpleColor(0xffffffff);
            copy.overlayTint = new SimpleColor(material.tints.texture);
            return copy;
        }

        ResourceLocation updatedLocation = Arrays.stream(material.textures)
                .filter(availableTextures::contains)
                .findFirst()
                .map(texture -> appendString(location, texture))
                .orElseGet(() -> appendString(location, availableTextures.get(0)));
        GridTextureModelData copy = copy();
        copy.location = updatedLocation;
        copy.tint = new SimpleColor(material.tints.texture);
        copy.overlayTint = new SimpleColor(material.tints.texture);
        return copy;
    }

    protected static ResourceLocation appendString(ResourceLocation resourceLocation, String string) {
        return ResourceLocation.fromNamespaceAndPath(resourceLocation.getNamespace(), resourceLocation.getPath() + string);
    }

    public GridTextureModelData withSlotSuffix(String suffix) {
        GridTextureModelData copy = copy();
        copy.location = ResourceLocation.fromNamespaceAndPath(location.getNamespace(), location.getPath() + suffix);
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
