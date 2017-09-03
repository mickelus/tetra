package se.mickelus.tetra.client.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.common.model.TRSRTransformation;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;
import se.mickelus.tetra.NBTHelper;
import se.mickelus.tetra.client.model.crap.BakedModuleModel;
import se.mickelus.tetra.client.model.crap.BasicBakedModel;

public class BakedModularModel implements IPerspectiveAwareModel {

    protected BakedModuleModel[] modules;
    protected final ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms;
//    protected final ImmutableList<BakedToolModelOverride> overrides;

    public BakedModularModel(BakedModuleModel[] parts,
                             ImmutableMap<TransformType, TRSRTransformation> transform) {

        this.modules = parts;
        this.transforms = transform;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        return null;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return null;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return null;
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return ToolItemOverrideList.INSTANCE;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType) {
        return null;
    }

    protected static class ToolItemOverrideList extends ItemOverrideList {

        private Cache<CacheKey, IBakedModel> bakedModelCache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();

        static ToolItemOverrideList INSTANCE = new ToolItemOverrideList();

        protected ToolItemOverrideList() {
            super(ImmutableList.of());
        }

        @Nonnull
        @Override
        public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, final ItemStack stack, final World world, final EntityLivingBase entity) {
            NBTTagCompound baseTag = NBTHelper.getTag(stack);
            IBakedModel outputModel = originalModel;
            if(!baseTag.hasNoTags()) {
                final BakedModularModel original = getBaseModel((BakedModularModel) originalModel, stack, world, entity);

                CacheKey key = getCacheKey(stack, original);

                try {
                    outputModel = bakedModelCache.get(key, () -> getCompleteModel(stack, world, entity, original));
                } catch(ExecutionException e) {
                    // do nothing, return original model
                }
            }
            return outputModel;
        }

        protected CacheKey getCacheKey(ItemStack stack, BakedModularModel original) {
            return new CacheKey(original, stack);
        }

        protected IBakedModel getCompleteModel(ItemStack stack, World world, EntityLivingBase entity, BakedModularModel original) {
            // get the texture for each part
            ImmutableList.Builder<BakedQuad> quads = ImmutableList.builder();

            addPartQuads(stack, original, quads);
//            addModifierQuads(stack, original, quads);
//            addExtraQuads(stack, original, quads, world, entity);

            return new BasicBakedModel(quads.build(), original.transforms, original);
        }

        private BakedModularModel getBaseModel(@Nonnull BakedModularModel original, ItemStack stack, World world, EntityLivingBase entity) {
            // check for an override
//            for(BakedToolModelOverride override : original.overrides) {
//                if(override.matches(stack, world, entity)) {
//                    original = override.bakedToolModel;
//                }
//            }
            return original;
        }

        private void addPartQuads(ItemStack stack, BakedModularModel original, ImmutableList.Builder<BakedQuad> quads) {
//            NBTTagList materials = TagUtil.getBaseMaterialsTagList(stack);
//            boolean broken = ToolHelper.isBroken(stack);
//
//            BakedModuleModel parts[] = original.modules;
//
//            // the model for the part of the given material. Broken or not-broken
//            for(int i = 0; i < parts.length; i++) {
//                String id = materials.getStringTagAt(i);
//
//                IBakedModel partModel;
//                if(broken && brokenParts[i] != null) {
//                    partModel = brokenParts[i].getModelByIdentifier(id);
//                }
//                else {
//                    partModel = parts[i].getModelByIdentifier(id);
//                }
//
//                quads.addAll(partModel.getQuads(null, null, 0));
//            }
        }

//        private void addModifierQuads(ItemStack stack, BakedToolModel original, ImmutableList.Builder<BakedQuad> quads) {
//            NBTTagList modifiers = TagUtil.getBaseModifiersTagList(stack);
//            Map<String, IBakedModel> modifierParts = original.modifierParts;
//            for(int i = 0; i < modifiers.tagCount(); i++) {
//                String modId = modifiers.getStringTagAt(i);
//                IBakedModel modModel = modifierParts.get(modId);
//                if(modModel != null) {
//                    quads.addAll(modModel.getQuads(null, null, 0));
//                }
//            }
//        }


        protected void addExtraQuads(ItemStack stack, BakedModularModel original, ImmutableList.Builder<BakedQuad> quads, World world, EntityLivingBase entity) {
            // for custom stuff
        }
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
            return data != null ? data.equals(cacheKey.data) : cacheKey.data == null;

        }

        @Override
        public int hashCode() {
            int result = parent != null ? parent.hashCode() : 0;
            result = 31 * result + (data != null ? data.hashCode() : 0);
            return result;
        }
    }

}