package se.mickelus.tetra.client.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.model.GridTextureModelData;
import se.mickelus.tetra.util.ItemStackTagHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public class ModularOverrideList extends ItemOverrides {
    private static final Logger logger = LogManager.getLogger();

    private final Cache<CacheKey, BakedModel> bakedModelCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();


    private final UnresolvedItemModel model;
    private final IGeometryBakingContext context;
    private final ModelBaker baker;
    private final Function<Material, TextureAtlasSprite> spriteGetter;
    private final ModelState modelState;
    private final ResourceLocation modelLocation;

    public ModularOverrideList(UnresolvedItemModel model, IGeometryBakingContext context, ModelBaker baker,
            Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ResourceLocation modelLocation) {
        this.model = model;
        this.context = context;
        this.baker = baker;
        this.spriteGetter = spriteGetter;
        this.modelState = modelState;
        this.modelLocation = modelLocation;
    }

    public void clearCache() {
        logger.debug("Clearing item model cache for " + modelLocation);
        bakedModelCache.invalidateAll();
    }

    @Nullable
    @Override
    public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int renderId) {
        CompoundTag baseTag = ItemStackTagHelper.getTag(stack);
        BakedModel result = originalModel;

        if (baseTag != null && !baseTag.isEmpty()) {
            CacheKey key = getCacheKey(stack, entity, originalModel);

            try {
                result = bakedModelCache.get(key, () -> getOverrideModel(stack, world, entity));
            } catch (ExecutionException e) {
                // do nothing, return original model
                e.printStackTrace();
            }
        }
        return result;
    }

    protected BakedModel getOverrideModel(ItemStack itemStack, @Nullable Level world, @Nullable LivingEntity entity) {
        IModularItem item = (IModularItem) itemStack.getItem();
        String transformVariant = item.getTransformVariant(itemStack, entity);
        ItemTransforms cameraTransforms = model.getCameraTransforms(transformVariant);
        BakingContextWrapper contextWrapper = new BakingContextWrapper(context, cameraTransforms);

        List<GridTextureModelData> models = item.getModels(itemStack, entity).stream()
                .filter(model -> model instanceof GridTextureModelData)
                .map(model -> (GridTextureModelData) model)
                .toList();

        Set<ItemDisplayContext> contexts = models.stream()
                .map(GridTextureModelData::getContexts)
                .filter(Objects::nonNull)
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        ItemLayerModel model = createLayerModel(filterModels(models, null));

        if (!contexts.isEmpty()) {
            var perspectiveModels = contexts.stream()
                    .collect(Collectors.toUnmodifiableMap(p -> p, p -> createLayerModel(filterModels(models, p))));
            var transformsModel = new TetraSeparateTransformsModel(model, perspectiveModels);
            return transformsModel.bake(contextWrapper, baker, spriteGetter, modelState, ItemOverrides.EMPTY);
        }

        return model.bake(contextWrapper, baker, spriteGetter, modelState, ItemOverrides.EMPTY);
    }

    protected ItemLayerModel createLayerModel(List<GridTextureModelData> models) {
        ImmutableList<Material> textures = models.stream()
                .map(moduleModel -> new Material(TextureAtlas.LOCATION_BLOCKS, moduleModel.getLocation()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));

        var renderTypes = new Int2ObjectOpenHashMap<ResourceLocation>();
        var builder = new QuadTransformerBuilder();
        for (int i = 0; i < models.size(); i++) {
            var model = models.get(i);
            if (model.getTint() != null && model.getTint().getRaw() != 0xffffffff) {
                builder.add(i, new ColorQuadTransformer(model.getTint().getRaw()));
            }
            if (model.getEmission() >= 0 && model.getEmission() < 16) {
                builder.add(i, QuadTransformers.settingEmissivity(model.getEmission()));
            }
            if (model.getTransform() != null) {
                builder.add(i, QuadTransformers.applying(model.getTransform()));
            }
            if (model.getRenderType() != null) {
                renderTypes.put(i, model.getRenderType());
            }
        }

        return new ItemLayerModel(textures, builder.get(), renderTypes);
    }

    protected List<GridTextureModelData> filterModels(List<GridTextureModelData> models, @Nullable ItemDisplayContext context) {
        return models.stream()
                .filter(model -> model.getContexts() == null || ArrayUtils.contains(model.getContexts(), context) != model.isInvertPerspectives())
                .toList();
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
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CacheKey cacheKey = (CacheKey) o;

            if (parent != null ? parent != cacheKey.parent : cacheKey.parent != null) {
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
