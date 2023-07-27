package se.mickelus.tetra.client.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.SeparateTransformsModel;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class TetraSeparateTransformsModel implements IUnbakedGeometry<net.minecraftforge.client.model.ItemLayerModel> {
    private final ItemLayerModel baseModel;
    private final Map<ItemTransforms.TransformType, ItemLayerModel> perspectives;

    public TetraSeparateTransformsModel(ItemLayerModel baseModel, Map<ItemTransforms.TransformType, ItemLayerModel> perspectives) {
        this.baseModel = baseModel;
        this.perspectives = perspectives;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
        return new SeparateTransformsModel.Baked(
                context.useAmbientOcclusion(), context.isGui3d(), context.useBlockLight(),
                spriteGetter.apply(context.getMaterial("particle")), overrides,
                baseModel.bake(context, bakery, spriteGetter, modelState, overrides, modelLocation),
                ImmutableMap.copyOf(Maps.transformValues(perspectives, value -> value.bake(context, bakery, spriteGetter, modelState, overrides, modelLocation)))
        );
    }

    @Override
    public Collection<Material> getMaterials(IGeometryBakingContext context, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        Set<Material> textures = Sets.newHashSet();
        if (context.hasMaterial("particle"))
            textures.add(context.getMaterial("particle"));
        textures.addAll(baseModel.getMaterials(context, modelGetter, missingTextureErrors));
        for (ItemLayerModel model : perspectives.values())
            textures.addAll(model.getMaterials(context, modelGetter, missingTextureErrors));
        return textures;
    }
}
