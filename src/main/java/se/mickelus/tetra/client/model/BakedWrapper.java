package se.mickelus.tetra.client.model;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.common.model.IModelState;

import java.util.List;
import java.util.function.Function;

import javax.annotation.Nonnull;

public class BakedWrapper implements IBakedModel {

    protected final IBakedModel parent;
    private final IModelState state;
    private final VertexFormat format;
    private final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter;

    public BakedWrapper(IModelState state, VertexFormat format, ItemOverrideList overrideList,
            Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        parent = new ItemLayerModel(ImmutableList.of(), overrideList).bake(state, format, bakedTextureGetter);
        this.state = state;
        this.format = format;
        this.bakedTextureGetter = bakedTextureGetter;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, long rand) {
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

    public IModelState getOriginalState() {
        return state;
    }

    public VertexFormat getOriginalFormat() {
        return format;
    }

    public Function<ResourceLocation, TextureAtlasSprite> getBakedTextureGetter() {
        return bakedTextureGetter;
    }
}