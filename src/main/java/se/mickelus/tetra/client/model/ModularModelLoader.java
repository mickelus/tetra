package se.mickelus.tetra.client.model;

import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import org.apache.commons.io.IOUtils;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.module.ItemUpgradeRegistry;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ModularModelLoader implements ICustomModelLoader {
    private IResourceManager resourceManager;

    public static ModularModelLoader instance = new ModularModelLoader();

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return modelLocation.getResourceDomain().equals(TetraMod.MOD_ID)
                && !(modelLocation instanceof ModelResourceLocation)
                && modelLocation.getResourcePath().contains("modular");
    }

    @Override
    public IModel loadModel(ResourceLocation location) throws Exception {

        ModelBlock modelBlock = loadModelBlock(getModelLocation(location));
        return new ModularModel(modelBlock);
    }

    public ModelBlock loadModelBlock(ResourceLocation location) throws Exception {
        IResource resource = null;
        Reader reader = null;
        ModelBlock modelBlock;

        try {
            resource = this.resourceManager.getResource(location);
            reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            modelBlock = ModelBlock.deserialize(reader);
            modelBlock.name = location.toString();
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(resource);
        }

        if (modelBlock.getParentLocation() != null) {
            modelBlock.parent = loadModelBlock(modelBlock.getParentLocation());
        }

        return modelBlock;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public void registerSprites() {

    }

    private ResourceLocation getModelLocation(ResourceLocation location) {
        return new ResourceLocation(location.getResourceDomain(), location.getResourcePath() + ".json");
    }
}
