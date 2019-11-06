package se.mickelus.tetra.client.model;

import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import org.apache.commons.io.IOUtils;
import se.mickelus.tetra.TetraMod;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class ModularModelLoader implements ICustomModelLoader {
    private IResourceManager resourceManager;

    public static ModularModelLoader instance = new ModularModelLoader();

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return modelLocation.getNamespace().equals(TetraMod.MOD_ID)
                && !(modelLocation instanceof ModelResourceLocation)
                && modelLocation.getPath().contains("modular");
    }

    @Override
    public IUnbakedModel loadModel(ResourceLocation location) throws Exception {

        BlockModel modelBlock = loadBlockModel(getModelLocation(location));
        return new ModularModel(modelBlock);
    }

    public BlockModel loadBlockModel(ResourceLocation location) throws Exception {
        IResource resource = null;
        Reader reader = null;
        BlockModel modelBlock;

        try {
            resource = this.resourceManager.getResource(location);
            reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            modelBlock = BlockModel.deserialize(reader);
            modelBlock.name = location.toString();
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(resource);
        }

        if (modelBlock.getParentLocation() != null) {
            modelBlock.parent = loadBlockModel(modelBlock.getParentLocation());
        }

        return modelBlock;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    private ResourceLocation getModelLocation(ResourceLocation location) {
        return new ResourceLocation(location.getNamespace(), location.getPath() + ".json");
    }
}
