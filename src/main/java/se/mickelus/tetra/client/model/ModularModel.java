package se.mickelus.tetra.client.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import se.mickelus.tetra.module.ItemUpgradeRegistry;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ModularModel implements IModel {
    private final ModelBlock modelBlock;

    public ModularModel(ModelBlock modelBlock) {
        this.modelBlock = modelBlock;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return ItemUpgradeRegistry.instance.getAllModules().stream()
                .flatMap((itemModule -> Arrays.stream(itemModule.getAllTextures())))
                .collect(Collectors.toList());
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        if(modelBlock == null) {
            return null; // todo: return "missing model" model
        }

        ItemCameraTransforms transforms = modelBlock.getAllTransforms();
        Map<ItemCameraTransforms.TransformType, TRSRTransformation> tMap = Maps.newHashMap();
        tMap.putAll(PerspectiveMapWrapper.getTransforms(transforms));
        tMap.putAll(PerspectiveMapWrapper.getTransforms(state));
        IModelState perState = new SimpleModelState(ImmutableMap.copyOf(tMap));

        return new BakedWrapper(perState, format, ModularOverrideList.INSTANCE, bakedTextureGetter);
    }

    @Override
    public IModelState getDefaultState() {
        return ModelRotation.X0_Y0;
    }

    @Override
    public ModularModel retexture(ImmutableMap<String, String> textures) {
        return this;
    }
}
