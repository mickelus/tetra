package se.mickelus.tetra.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.ISprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ItemLayerModel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class BakedWrapper implements IBakedModel {

    protected final IBakedModel parent;
    private final ModelBakery bakery;

    private final Function<ResourceLocation, TextureAtlasSprite> spriteGetter;
    private final ISprite sprite;
    private final VertexFormat format;

    public BakedWrapper(ModelBakery bakery, Function<ResourceLocation, TextureAtlasSprite> spriteGetter, ISprite sprite,
            VertexFormat format) {
        parent = new ItemLayerModel(ImmutableList.of(), ModularOverrideList.INSTANCE)
                .bake(bakery, spriteGetter, sprite, format);

        this.bakery = bakery;
        this.spriteGetter = spriteGetter;
        this.sprite = sprite;
        this.format = format;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
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
    public ItemOverrideList getOverrides() {
        return parent.getOverrides();
    }

    public ModelBakery getBakery() {
        return bakery;
    }

    public Function<ResourceLocation, TextureAtlasSprite> getSpriteGetter() {
        return spriteGetter;
    }

    public ISprite getSprite() {
        return sprite;
    }

    public VertexFormat getFormat() {
        return format;
    }
}