package se.mickelus.tetra.client.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.model.IModelConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.data.ModuleModel;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class ModularOverrideList extends ItemOverrides {
    private static final Logger logger = LogManager.getLogger();

    private Cache<CacheKey, BakedModel> bakedModelCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();


    private ModularItemModel model;
    private IModelConfiguration owner;
    private ModelBakery bakery;
    private Function<Material, TextureAtlasSprite> spriteGetter;
    private ModelState modelTransform;
    private ResourceLocation modelLocation;

    public ModularOverrideList(ModularItemModel model, IModelConfiguration owner, ModelBakery bakery,
            Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ResourceLocation modelLocation) {
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
    public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity) {
        CompoundTag baseTag = stack.getTag();
        BakedModel result = originalModel;

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

    protected BakedModel getOverrideModel(ItemStack itemStack, @Nullable Level world, @Nullable LivingEntity entity) {
        IModularItem item  = (IModularItem) itemStack.getItem();

        List<ModuleModel> models = item.getModels(itemStack, entity);
        String transformVariant = item.getTransformVariant(itemStack, entity);

        return model.realBake(models, transformVariant, owner, bakery, spriteGetter, modelTransform, ItemOverrides.EMPTY, modelLocation);
    }

    protected CacheKey getCacheKey(ItemStack itemStack, LivingEntity entity, BakedModel original) {
        return new CacheKey(original, ((IModularItem) itemStack.getItem()).getModelCacheKey(itemStack, entity));
    }

    protected static class CacheKey {

        final BakedModel parent;
        final String data;

        protected CacheKey(BakedModel parent, String hash) {
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
