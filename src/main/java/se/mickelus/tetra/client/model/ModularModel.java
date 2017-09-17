package se.mickelus.tetra.client.model;

import com.google.common.collect.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import se.mickelus.tetra.module.ItemUpgradeRegistry;

import java.util.*;
import java.util.stream.Collectors;

public class ModularModel implements IRetexturableModel {
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
    public IBakedModel bake(IModelState state, VertexFormat format, com.google.common.base.Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        if(modelBlock == null) {
            return null; // todo: return "missing model" model
        }

        ItemCameraTransforms transforms = modelBlock.getAllTransforms();
        Map<ItemCameraTransforms.TransformType, TRSRTransformation> tMap = Maps.newHashMap();
        tMap.putAll(IPerspectiveAwareModel.MapWrapper.getTransforms(transforms));
        tMap.putAll(IPerspectiveAwareModel.MapWrapper.getTransforms(state));
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
