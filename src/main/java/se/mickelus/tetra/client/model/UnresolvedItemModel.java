package se.mickelus.tetra.client.model;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.client.textures.UnitTextureAtlasSprite;

import java.util.*;
import java.util.function.Function;

public final class UnresolvedItemModel implements IUnbakedGeometry<UnresolvedItemModel> {

    private final ItemTransforms cameraTransforms;
    ModularOverrideList overrideList;
    private Map<String, ItemTransforms> transformVariants = Collections.emptyMap();

    public UnresolvedItemModel(ItemTransforms cameraTransforms, Map<String, ItemTransforms> transformVariants) {
        this(cameraTransforms);
        this.transformVariants = transformVariants != null ? transformVariants : Collections.emptyMap();
    }

    public UnresolvedItemModel(ItemTransforms cameraTransforms) {
        this.cameraTransforms = cameraTransforms;
    }

    protected ItemTransforms getCameraTransforms(String transformVariant) {
        if (transformVariant != null && transformVariants.containsKey(transformVariant)) {
            ItemTransforms variant = transformVariants.get(transformVariant);

            return new ItemTransforms(
                    variant.hasTransform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND) ?
                            variant.thirdPersonLeftHand : cameraTransforms.thirdPersonLeftHand,
                    variant.hasTransform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) ?
                            variant.thirdPersonRightHand : cameraTransforms.thirdPersonRightHand,
                    variant.hasTransform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND) ?
                            variant.firstPersonLeftHand : cameraTransforms.firstPersonLeftHand,
                    variant.hasTransform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) ?
                            variant.firstPersonRightHand : cameraTransforms.firstPersonRightHand,
                    variant.hasTransform(ItemDisplayContext.HEAD) ?
                            variant.head : cameraTransforms.head,
                    variant.hasTransform(ItemDisplayContext.GUI) ?
                            variant.gui : cameraTransforms.gui,
                    variant.hasTransform(ItemDisplayContext.GROUND) ?
                            variant.ground : cameraTransforms.ground,
                    variant.hasTransform(ItemDisplayContext.FIXED) ?
                            variant.fixed : cameraTransforms.fixed,
                    variant.moddedTransforms
            );
        }

        return cameraTransforms;
    }

    public void clearCache() {
        Optional.ofNullable(overrideList).ifPresent(ModularOverrideList::clearCache);
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
        overrideList = new ModularOverrideList(this, context, baker, spriteGetter, modelState, modelLocation);
        return new Baked(overrideList);
    }

    private static class Baked extends SimpleBakedModel {
        private static final Material MISSING_TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation());

        public Baked(ItemOverrides itemOverrideList) {
            super(List.of(), Map.of(), false, false, false, UnitTextureAtlasSprite.INSTANCE, ItemTransforms.NO_TRANSFORMS,
                    itemOverrideList, RenderTypeGroup.EMPTY);
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return MISSING_TEXTURE.sprite();
        }

        @Override
        public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
            return List.of();
        }
    }
}
