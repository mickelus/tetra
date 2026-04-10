package se.mickelus.tetra.client.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.SeparateTransformsModel;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.util.Map;
import java.util.function.Function;

public class TetraSeparateTransformsModel implements IUnbakedGeometry<TetraSeparateTransformsModel> {
    private final ItemLayerModel baseModel;
    private final Map<ItemDisplayContext, ItemLayerModel> contextModels;

    public TetraSeparateTransformsModel(ItemLayerModel baseModel, Map<ItemDisplayContext, ItemLayerModel> contextModels) {
        this.baseModel = baseModel;
        this.contextModels = contextModels;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
        return new SeparateTransformsModel.Baked(
                context.useAmbientOcclusion(), context.isGui3d(), context.useBlockLight(),
                spriteGetter.apply(context.getMaterial("particle")), overrides,
                baseModel.bake(context, bakery, spriteGetter, modelState, overrides),
                ImmutableMap.copyOf(Maps.transformValues(contextModels, value -> value.bake(context, bakery, spriteGetter, modelState, overrides)))
        );
    }
}
