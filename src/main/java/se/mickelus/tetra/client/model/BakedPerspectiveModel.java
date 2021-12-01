package se.mickelus.tetra.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.client.model.BakedItemModel;
import net.minecraftforge.client.model.PerspectiveMapWrapper;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class BakedPerspectiveModel extends BakedItemModel {
    private ItemTransforms cameraTransforms;
    public BakedPerspectiveModel(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle,
            ImmutableMap<ItemTransforms.TransformType, Transformation> transforms, ItemOverrides overrides,
            boolean untransformed, boolean isSideLit, ItemTransforms cameraTransforms) {
        super(quads, particle, transforms, overrides, untransformed, isSideLit);

        this.cameraTransforms = cameraTransforms;
    }

    @Override
    public BakedModel handlePerspective(ItemTransforms.TransformType type, PoseStack mat) {
        if (cameraTransforms != null) {
            return net.minecraftforge.client.ForgeHooksClient.handlePerspective(this, type, mat);
        }
        return PerspectiveMapWrapper.handlePerspective(this, transforms, type, mat);
    }

    @Override
    public boolean doesHandlePerspectives() {
        return true;
    }

    @Override
    public ItemTransforms getTransforms() {
        return cameraTransforms;
    }
}
