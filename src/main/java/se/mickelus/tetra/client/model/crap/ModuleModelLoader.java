package se.mickelus.tetra.client.model.crap;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import se.mickelus.tetra.TetraMod;

public class ModuleModelLoader implements ICustomModelLoader {
    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return modelLocation.getResourceDomain().equals(TetraMod.MOD_ID) &&
                modelLocation.getResourcePath().contains("models/item/module");
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception {
        return null;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }
}
