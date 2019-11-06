package se.mickelus.tetra.client.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.ISprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import se.mickelus.tetra.module.ItemUpgradeRegistry;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ModularModel implements IUnbakedModel {
    private final BlockModel modelBlock;

    public ModularModel(BlockModel modelBlock) {
        this.modelBlock = modelBlock;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<ResourceLocation> getTextures(Function<ResourceLocation, IUnbakedModel> modelGetter, Set<String> missingTextureErrors) {
        return ItemUpgradeRegistry.instance.getAllModules().stream()
                .flatMap((itemModule -> Arrays.stream(itemModule.getAllTextures())))
                .collect(Collectors.toList());
    }

    @Nullable
    @Override
    public IBakedModel bake(ModelBakery bakery, Function<ResourceLocation, TextureAtlasSprite> spriteGetter, ISprite sprite, VertexFormat format) {
        if(modelBlock == null) {
            return null; // todo: return "missing model" model
        }

        // todo 1.14: do we no longer need to get a transform here?
//        ItemCameraTransforms transforms = modelBlock.getAllTransforms();
//        Map<ItemCameraTransforms.TransformType, TRSRTransformation> tMap = Maps.newHashMap();
//        tMap.putAll(PerspectiveMapWrapper.getTransforms(transforms));
//        tMap.putAll(PerspectiveMapWrapper.getTransforms(sprite.getState()));
//        IModelState perState = new SimpleModelState(ImmutableMap.copyOf(tMap));

        return new BakedWrapper(bakery, spriteGetter, sprite, format);
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
