package se.mickelus.tetra.client.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModelConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.module.data.ModuleModel;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

// todo: 1.15: ripped everything out
public class ModularOverrideList extends ItemOverrideList {
    private static final Logger logger = LogManager.getLogger();

    private Cache<CacheKey, IBakedModel> bakedModelCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();


    private ModularItemModel model;
    private IModelConfiguration owner;
    private ModelBakery bakery;
    private Function<RenderMaterial, TextureAtlasSprite> spriteGetter;
    private IModelTransform modelTransform;
    private ResourceLocation modelLocation;

    public ModularOverrideList(ModularItemModel model, IModelConfiguration owner, ModelBakery bakery,
            Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ResourceLocation modelLocation) {
        this.model = model;
        this.owner = owner;
        this.bakery = bakery;
        this.spriteGetter = spriteGetter;
        this.modelTransform = modelTransform;
        this.modelLocation = modelLocation;
    }

    public void clearCache() {
        logger.debug("Clearing item model cache for " + modelLocation);
        bakedModelCache.invalidateAll();
    }

    @Nullable
    @Override
    public IBakedModel getOverrideModel(IBakedModel originalModel, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity) {
        CompoundNBT baseTag = stack.getTag();
        IBakedModel result = originalModel;

        if(baseTag != null && !baseTag.isEmpty()) {
            CacheKey key = getCacheKey(stack, entity, originalModel);

            try {
                result = bakedModelCache.get(key, () -> getOverrideModel(stack, world, entity));
            } catch(ExecutionException e) {
                // do nothing, return original model
                e.printStackTrace();
            }
        }
        return result;
    }

    protected IBakedModel getOverrideModel(ItemStack itemStack, @Nullable World world, @Nullable LivingEntity entity) {
        ModularItem item  = (ModularItem) itemStack.getItem();

        List<ModuleModel> models = item.getModels(itemStack, entity);
        String transformVariant = item.getTransformVariant(itemStack, entity);

        return model.realBake(models, transformVariant, owner, bakery, spriteGetter, modelTransform, ItemOverrideList.EMPTY, modelLocation);
    }

    protected CacheKey getCacheKey(ItemStack itemStack, LivingEntity entity, IBakedModel original) {
        return new CacheKey(original, ((ModularItem) itemStack.getItem()).getModelCacheKey(itemStack, entity));
    }

    protected static class CacheKey {

        final IBakedModel parent;
        final String data;

        protected CacheKey(IBakedModel parent, String hash) {
            this.parent = parent;
            this.data = hash;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) {
                return true;
            }
            if(o == null || getClass() != o.getClass()) {
                return false;
            }

            CacheKey cacheKey = (CacheKey) o;

            if(parent != null ? parent != cacheKey.parent : cacheKey.parent != null) {
                return false;
            }
            return Objects.equals(data, cacheKey.data);

        }

        @Override
        public int hashCode() {
            int result = parent != null ? parent.hashCode() : 0;
            result = 31 * result + (data != null ? data.hashCode() : 0);
            return result;
        }
    }
}
