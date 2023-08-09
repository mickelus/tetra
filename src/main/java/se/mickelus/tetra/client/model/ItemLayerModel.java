package se.mickelus.tetra.client.model;

import com.mojang.math.Transformation;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.ForgeRenderTypes;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.model.CompositeModel;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.client.model.geometry.UnbakedGeometryHelper;

import java.util.List;
import java.util.function.Function;

public class ItemLayerModel implements IUnbakedGeometry<ItemLayerModel> {
    private final Int2ObjectMap<List<IQuadTransformer>> layerTransformers;
    private final Int2ObjectMap<ResourceLocation> renderTypeNames;
    private List<Material> textures;

    public ItemLayerModel(List<Material> textures, Int2ObjectMap<List<IQuadTransformer>> layerTransformers, Int2ObjectMap<ResourceLocation> renderTypeNames) {
        this.textures = textures;
        this.layerTransformers = layerTransformers;
        this.renderTypeNames = renderTypeNames;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
            ItemOverrides overrides, ResourceLocation modelLocation) {
        TextureAtlasSprite particle = spriteGetter.apply(context.hasMaterial("particle")
                ? context.getMaterial("particle")
                : textures.size() > 0
                ? textures.get(0)
                : new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation()));

        Transformation rootTransform = context.getRootTransform();
        if (!rootTransform.isIdentity()) {
            modelState = new SimpleModelState(modelState.getRotation().compose(rootTransform), modelState.isUvLocked());
        }

        CompositeModel.Baked.Builder builder = CompositeModel.Baked.builder(context, particle, overrides, context.getTransforms());
        for (int i = 0; i < textures.size(); i++) {
            TextureAtlasSprite sprite = spriteGetter.apply(textures.get(i));
            List<BlockElement> unbaked = UnbakedGeometryHelper.createUnbakedItemElements(i, sprite.contents());

            List<BakedQuad> quads = UnbakedGeometryHelper.bakeElements(unbaked, $ -> sprite, modelState, modelLocation);

            if (layerTransformers.containsKey(i)) {
                layerTransformers.get(i).forEach(transformer -> transformer.processInPlace(quads));
            }

            ResourceLocation renderTypeName = renderTypeNames.get(i);
            RenderTypeGroup renderTypes = renderTypeName != null
                    ? context.getRenderType(renderTypeName)
                    : new RenderTypeGroup(RenderType.solid(), ForgeRenderTypes.ITEM_UNSORTED_TRANSLUCENT.get());
            builder.addQuads(renderTypes, quads);
        }

        return builder.build();
    }
}

