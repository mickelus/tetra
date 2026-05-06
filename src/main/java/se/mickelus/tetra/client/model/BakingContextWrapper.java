package se.mickelus.tetra.client.model;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.Nullable;

public class BakingContextWrapper implements IGeometryBakingContext {
    private IGeometryBakingContext parent;
    private ItemTransforms transforms;


    public BakingContextWrapper(IGeometryBakingContext parent, ItemTransforms transforms) {
        this.parent = parent;
        this.transforms = transforms;
    }

    @Override
    public String getModelName() {
        return parent.getModelName();
    }

    @Override
    public boolean hasMaterial(String name) {
        return parent.hasMaterial(name);
    }

    @Override
    public Material getMaterial(String name) {
        return parent.getMaterial(name);
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean useBlockLight() {
        return parent.useBlockLight();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return parent.useAmbientOcclusion();
    }

    @Override
    public ItemTransforms getTransforms() {
        return transforms;
    }

    @Override
    public Transformation getRootTransform() {
        return parent.getRootTransform();
    }

    @Override
    @Nullable
    public ResourceLocation getRenderTypeHint() {
        return parent.getRenderTypeHint();
    }

    @Override
    public boolean isComponentVisible(String component, boolean fallback) {
        return parent.isComponentVisible(component, fallback);
    }
}
