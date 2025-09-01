package se.mickelus.tetra.module.model;

import com.mojang.math.Transformation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import se.mickelus.tetra.module.Priority;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public abstract class AbstractTextureModel implements IModuleModel {
    public String type = "item";
    protected ResourceLocation location;
    protected ResourceLocation renderType;
    protected Transformation transform;
    protected int emission = 0;
    protected int tint = 0xffffffff;
    protected int overlayTint = 0xffffffff;

    protected Priority renderLayer = Priority.BASE;

    protected boolean invertPerspectives = false;
    protected ItemDisplayContext[] contexts;

    public ResourceLocation getLocation() {
        return location;
    }

    public ResourceLocation getRenderType() {
        return renderType;
    }

    public Transformation getTransform() {
        return transform;
    }

    public int getEmission() {
        return emission;
    }

    public int getTint() {
        return tint;
    }

    public int getOverlayTint() {
        return overlayTint;
    }

    @Override
    public Priority getRenderLayer() {
        return renderLayer;
    }

    public boolean isInvertPerspectives() {
        return invertPerspectives;
    }

    public ItemDisplayContext[] getContexts() {
        return contexts;
    }
}
