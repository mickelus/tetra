package se.mickelus.tetra.client.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import se.mickelus.tetra.NBTHelper;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.data.ModuleModel;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ModularOverrideList extends ItemOverrideList {

    private Cache<CacheKey, IBakedModel> bakedModelCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();


    private final ModelBakery bakery;
    private final BlockModel unbaked;

    public ModularOverrideList(final ModelBakery bakery, final BlockModel unbaked) {
        super();

        this.bakery = bakery;
        this.unbaked = unbaked;
    }

    public void clearCache() {
        bakedModelCache.invalidateAll();
    }

    @Nullable
    @Override
    public IBakedModel getModelWithOverrides(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable LivingEntity entity) {
        CompoundNBT baseTag = NBTHelper.getTag(stack);
        IBakedModel result = originalModel;
        if(!baseTag.isEmpty()) {
            CacheKey key = getCacheKey(stack, originalModel);

            try {
                result = bakedModelCache.get(key, () -> getOverrideModel(stack));
            } catch(ExecutionException e) {
                // do nothing, return original model
                e.printStackTrace();
            }
        }
        return result;
    }

    protected CacheKey getCacheKey(ItemStack stack, IBakedModel original) {
        return new CacheKey(original, stack);
    }

    protected IBakedModel getOverrideModel(ItemStack itemStack) {
        ItemModular item  = (ItemModular) itemStack.getItem();

        // todo: look at ItemModelGenerator
        ItemCameraTransforms transforms = unbaked.getAllTransforms();
        Map<ItemCameraTransforms.TransformType, TRSRTransformation> tMap = Maps.newHashMap();
        tMap.putAll(PerspectiveMapWrapper.getTransforms(transforms));
        // tMap.putAll(PerspectiveMapWrapper.getTransforms(new BasicState(unbaked.getDefaultState(), false).getState()));
        SimpleModelState perState = new SimpleModelState(ImmutableMap.copyOf(tMap));

//        Map<String, String> textures = new HashMap<>();
//        item.getTextures(itemStack).forEach(resourceLocation -> textures.put("", resourceLocation.toString()));
//        unbaked.retexture(ImmutableMap.copyOf(textures));
//
//        return unbaked.bake(bakery, ModelLoader.defaultTextureGetter(), new BasicState(unbaked.getDefaultState(), false),
//                DefaultVertexFormats.ITEM);

        List<ModuleModel> models = item.getModels(itemStack);

         return new ModularItemModel(models).bake(bakery, ModelLoader.defaultTextureGetter(),
                 perState, DefaultVertexFormats.ITEM);
    }

    protected static class CacheKey {

        final IBakedModel parent;
        final String data;

        protected CacheKey(IBakedModel parent, ItemStack stack) {
            this.parent = parent;
            this.data = getDataFromStack(stack);
        }

        protected String getDataFromStack(ItemStack stack) {
            return NBTHelper.getTag(stack).toString();
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
