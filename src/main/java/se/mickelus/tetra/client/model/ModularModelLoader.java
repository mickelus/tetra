package se.mickelus.tetra.client.model;

import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ItemLayerModel;
import org.apache.commons.io.IOUtils;
import se.mickelus.tetra.TetraMod;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ModularModelLoader implements ICustomModelLoader {
    private IResourceManager resourceManager;

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return modelLocation.getResourceDomain().equals(TetraMod.MOD_ID)
                && !(modelLocation instanceof ModelResourceLocation)
                && modelLocation.getResourcePath().contains("modular");
    }

    @Override
    public IModel loadModel(ResourceLocation location) throws Exception {
        IResource resource = null;
        Reader reader = null;
        ModelBlock blockModel;

        try {
            resource = this.resourceManager.getResource(this.getModelLocation(location));
            reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            blockModel = ModelBlock.deserialize(reader);
            blockModel.name = location.toString();
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(resource);
        }

//        return new ModularModel(blockModel);
        return new ItemLayerModel(blockModel);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    private ResourceLocation getModelLocation(ResourceLocation location) {
        return new ResourceLocation(location.getResourceDomain(), location.getResourcePath() + ".json");
    }
}
