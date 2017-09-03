package se.mickelus.tetra.client.model.crap;

import com.google.common.collect.ImmutableMap;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraftforge.common.model.TRSRTransformation;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * This class represents something that has a single material. The base model is the default without a material. The
 * modules represent the different materials. Tools etc. are built out of multiple of these models
 *
 * ..basically it's a simple (Itemmeta -> Model) model
 */
public class BakedModuleModel extends BakedWrapper.Perspective {

    protected Map<String, IBakedModel> modules;

    public BakedModuleModel(IBakedModel base, ImmutableMap<TransformType, TRSRTransformation> transforms) {
        super(base, transforms);

        this.modules = new HashMap<>();
    }

    public void addModule(String key, IBakedModel model) {
        modules.put(key, model);
    }

    public IBakedModel getModelByIdentifier(String identifier) {
        IBakedModel materialModel = modules.get(identifier);
        if(materialModel == null) {
            return this;
        }

        return materialModel;
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}