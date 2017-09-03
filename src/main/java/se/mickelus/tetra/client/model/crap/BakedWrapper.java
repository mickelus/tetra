package se.mickelus.tetra.client.model.crap;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.common.model.TRSRTransformation;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;

public class BakedWrapper implements IBakedModel {

    protected final IBakedModel parent;

    public BakedWrapper(IBakedModel parent) {
        this.parent = parent;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        return parent.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return parent.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return parent.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return parent.isBuiltInRenderer();
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleTexture() {
        return parent.getParticleTexture();
    }

    @Nonnull
    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return parent.getItemCameraTransforms();
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return parent.getOverrides();
    }

    public static class Perspective extends BakedWrapper implements IPerspectiveAwareModel {
        protected final ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms;

        public Perspective(IBakedModel parent, ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms) {
            super(parent);
            this.transforms = transforms;
        }

        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
            return IPerspectiveAwareModel.MapWrapper.handlePerspective(this, transforms, cameraTransformType);
        }
    }
}