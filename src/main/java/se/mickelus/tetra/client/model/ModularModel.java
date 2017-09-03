package se.mickelus.tetra.client.model;

import com.google.common.collect.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

import javax.annotation.Nullable;
import java.util.*;

public class ModularModel implements IRetexturableModel, IModelSimpleProperties {

    private static final String EMPTY_MODEL_RAW = "{    'elements': [        {   'from': [0, 0, 0],            'to': [16, 16, 16],            'faces': {                'down': {'uv': [0, 0, 16, 16], 'texture': '' }            }        }    ]}".replaceAll("'", "\"");
    protected static final ModelBlock MODEL_GENERATED = ModelBlock.deserialize(EMPTY_MODEL_RAW);

    private final ModelBlock modelBlock;

    public ModularModel(ModelBlock modelBlock) {
        this.modelBlock = modelBlock;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return null;
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        if(modelBlock.getParentLocation() != null) {
            if(modelBlock.getParentLocation().getResourcePath().equals("builtin/generated")) {
                modelBlock.parent = MODEL_GENERATED;
            }
        }

        ImmutableSet.Builder<ResourceLocation> builder = ImmutableSet.builder();

        if(hasItemModel(modelBlock)) {
            for(String layer : ItemModelGenerator.LAYERS)
            {
                String texture = modelBlock.resolveTextureName(layer);
                ResourceLocation resourceLocation = new ResourceLocation(texture);
                if(!texture.equals(layer)) {
                    builder.add(resourceLocation);
                }
            }
        }
        for(String texture : modelBlock.textures.values())
        {
            if(!texture.startsWith("#")) {
                builder.add(new ResourceLocation(texture));
            }
        }
        return builder.build();
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, com.google.common.base.Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        if(modelBlock == null) {
            return null; // todo: return "missing model" model
        }

        List<TRSRTransformation> newTransforms = Lists.newArrayList();
        for(int i = 0; i < modelBlock.getElements().size(); i++)
        {
            BlockPart part = modelBlock.getElements().get(i);
//            newTransforms.add(animation.getPartTransform(state, part, i));
        }

        ItemCameraTransforms transforms = modelBlock.getAllTransforms();
        Map<ItemCameraTransforms.TransformType, TRSRTransformation> tMap = Maps.newHashMap();
        tMap.putAll(IPerspectiveAwareModel.MapWrapper.getTransforms(transforms));
        tMap.putAll(IPerspectiveAwareModel.MapWrapper.getTransforms(state));
        IModelState perState = new SimpleModelState(ImmutableMap.copyOf(tMap));

        return new ItemLayerModel(modelBlock).bake(perState, format, bakedTextureGetter);
    }

    @Override
    public IModelState getDefaultState() {
        return ModelRotation.X0_Y0;
    }

    private IModel getMissingModel() {
        return null;
    }

    @Override
    public ModularModel retexture(ImmutableMap<String, String> textures) {
        return this;
    }

    @Override
    public ModularModel smoothLighting(boolean value) {
        return this;
    }

    @Override
    public ModularModel gui3d(boolean value) {
        return this;
    }

    private boolean hasItemModel(@Nullable ModelBlock modelBlock) {
        if (modelBlock == null) {
            return false;
        } else {
            return modelBlock.getRootModel() == MODEL_GENERATED;
        }
    }
}
