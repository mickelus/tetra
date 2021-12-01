package se.mickelus.tetra.client.model;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;

public class BakedWrapper implements BakedModel {

    private TextureAtlasSprite particleAtlas;
    private ItemOverrides itemOverrideList;

    public BakedWrapper(ModularItemModel model, IModelConfiguration owner, ModelBakery bakery,
            Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ResourceLocation modelLocation,
            ItemOverrides itemOverrideList) {

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
    public ItemOverrides getOverrides() {
        return itemOverrideList;
    }

    @Override
    public boolean doesHandlePerspectives() {
        return false;
    }
}