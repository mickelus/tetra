package se.mickelus.tetra.items.modular.impl.shield;

import net.minecraft.resources.ResourceLocation;
import se.mickelus.mutil.gui.SimpleColor;
import se.mickelus.tetra.items.modular.ItemColors;
import se.mickelus.tetra.module.Priority;
import se.mickelus.tetra.module.model.IModuleModel;

import java.util.Arrays;
import java.util.List;

public class ShieldModuleModel implements IModuleModel {
    protected ResourceLocation type;
    protected ResourceLocation model;
    protected ResourceLocation texture;
    protected SimpleColor tint;
    protected SimpleColor overlayTint;
    protected Priority renderLayer = Priority.BASE;

    public ShieldModuleModel(ResourceLocation type, ResourceLocation model, ResourceLocation texture, SimpleColor tint, SimpleColor overlayTint,
            Priority renderLayer) {
        this.type = type;
        this.model = model;
        this.texture = texture;
        this.tint = tint;
        this.overlayTint = overlayTint;
        this.renderLayer = renderLayer;
    }

    @Override
    public ResourceLocation getType() {
        return type;
    }

    @Override
    public Priority getRenderLayer() {
        return renderLayer;
    }

    public ResourceLocation getModel() {
        return model;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public SimpleColor getTint() {
        return tint;
    }

    public SimpleColor getOverlayTint() {
        return overlayTint;
    }

    public ShieldModuleModel forMaterial(List<String> availableTextures, String[] modelOverrides, String[] materialTextures, boolean tintOverride,
            SimpleColor materialTint) {
        if (Arrays.stream(modelOverrides).anyMatch(override -> texture.getPath().equals(override))) {
            ShieldModuleModel copy = copy();
            copy.texture = appendString(texture, materialTextures[0]);
            copy.tint = tintOverride ? materialTint : new SimpleColor(0xffffffff);
            copy.overlayTint = materialTint;
            return copy;
        }

        ResourceLocation updatedLocation = Arrays.stream(materialTextures)
                .filter(availableTextures::contains)
                .findFirst()
                .map(texture -> appendString(this.texture, texture))
                .orElseGet(() -> appendString(this.texture, availableTextures.get(0)));
        ShieldModuleModel copy = copy();
        copy.texture = updatedLocation;
        copy.tint = materialTint;
        copy.overlayTint = materialTint;
        return copy;
    }

    protected static ResourceLocation appendString(ResourceLocation resourceLocation, String string) {
        return new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath() + string);
    }

    public ShieldModuleModel withSlotSuffix(String suffix) {
        ShieldModuleModel copy = copy();
        copy.texture = new ResourceLocation(texture.getNamespace(), texture.getPath() + suffix);
        return copy;
    }

    public ShieldModuleModel inheritTint(SimpleColor parentTint) {
        if (ItemColors.inherit == tint.getRaw()) {
            ShieldModuleModel copy = copy();
            copy.tint = parentTint;
            return copy;
        }
        return this;
    }

    protected ShieldModuleModel copy() {
        return new ShieldModuleModel(type, model, texture, tint, overlayTint, renderLayer);
    }
}
