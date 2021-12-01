package se.mickelus.tetra.client.model;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class BakedWrapper implements IBakedModel {

    private TextureAtlasSprite particleAtlas;
    private ItemOverrideList itemOverrideList;

    public BakedWrapper(ModularItemModel model, IModelConfiguration owner, ModelBakery bakery,
            Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ResourceLocation modelLocation,
            ItemOverrideList itemOverrideList) {

        particleAtlas = spriteGetter.apply(owner.resolveTexture("particle"));
        this.itemOverrideList = itemOverrideList;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return particleAtlas;
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return itemOverrideList;
    }

    @Override
    public boolean doesHandlePerspectives() {
        return false;
    }
}