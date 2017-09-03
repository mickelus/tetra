package se.mickelus.tetra.client.model.crap;

import com.google.common.collect.ImmutableList;
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

public class BasicBakedModel implements IPerspectiveAwareModel {

    private final List<BakedQuad> quads;
    private final ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms;
    private final TextureAtlasSprite particle;
    private final boolean ambientOcclusion;
    private final boolean isGui3d;
    private final ItemOverrideList overrides;

    public BasicBakedModel(ImmutableList<BakedQuad> quads, ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms, IBakedModel base) {
        this(
                (List<BakedQuad>)quads,
                transforms,
                base.getParticleTexture(),
                base.isAmbientOcclusion(),
                base.isGui3d(),
                base.getOverrides()
        );
    }

    public BasicBakedModel(List<BakedQuad> quads, ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms, IBakedModel base) {
        this(
                quads,
                transforms,
                base.getParticleTexture(),
                base.isAmbientOcclusion(),
                base.isGui3d(),
                base.getOverrides()
        );
    }

    public BasicBakedModel(List<BakedQuad> quads, ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms, TextureAtlasSprite particle, boolean ambientOcclusion, boolean isGui3d, ItemOverrideList overrides) {
        this.quads = quads;
        this.particle = particle;
        this.transforms = transforms;
        this.ambientOcclusion = ambientOcclusion;
        this.isGui3d = isGui3d;
        this.overrides = overrides;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        return quads;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return ambientOcclusion;
    }

    @Override
    public boolean isGui3d() {
        return isGui3d;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleTexture() {
        return particle;
    }

    @Nonnull
    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return overrides;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        return IPerspectiveAwareModel.MapWrapper.handlePerspective(this, transforms, cameraTransformType);
    }

    public static class Wrapper extends BasicBakedModel {

        private final IPerspectiveAwareModel parent;

        public Wrapper(ImmutableList<BakedQuad> quads, IPerspectiveAwareModel base) {
            super(quads, null, base);

            parent = base;
        }

        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
            Pair<? extends IBakedModel, Matrix4f> pair = parent.handlePerspective(cameraTransformType);
            return Pair.of(this, pair.getRight());
        }
    }
}