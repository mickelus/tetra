package se.mickelus.tetra.module.model;

import com.mojang.math.Transformation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import se.mickelus.mutil.gui.SimpleColor;
import se.mickelus.tetra.module.Priority;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public abstract class AbstractTextureModelData implements IModuleModel {
    protected ResourceLocation type;
    protected ResourceLocation location;
    protected ResourceLocation renderType;
    protected Transformation transform;
    protected int emission = 0;
    protected SimpleColor tint = new SimpleColor(0xffffffff);
    protected SimpleColor overlayTint;

    protected Priority renderLayer = Priority.BASE;

    protected boolean invertPerspectives = false;
    protected ItemDisplayContext[] contexts;

    AbstractTextureModelData() {
        this.tint = new SimpleColor(0xffffffff);
        this.renderLayer = Priority.BASE;
        this.invertPerspectives = false;
    }

    @Override
    public ResourceLocation getType() {
        return type;
    }

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

    public SimpleColor getTint() {
        return tint;
    }

    @Override
    public SimpleColor getOverlayTint() {
        return overlayTint != null ? overlayTint : tint;
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
