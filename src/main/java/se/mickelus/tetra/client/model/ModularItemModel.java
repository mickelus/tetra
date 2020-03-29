package se.mickelus.tetra.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.BakedItemModel;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.TRSRTransformer;
import se.mickelus.tetra.module.data.ModuleModel;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Forge reimplementation of vanilla {@link ItemModelGenerator}, i.e. builtin/generated models,
 * with the following changes:
 * - Represented as a true {@link IUnbakedModel} so it can be baked as usual instead of using
 *   special-case logic like vanilla does.
 * - Various fixes in the baking logic.
 * - Not limited to 4 layers maximum.
 */
public final class ModularItemModel implements IModelGeometry<ModularItemModel> {

    private ItemCameraTransforms cameraTransforms;

    ModularOverrideList overrideList;

    public ModularItemModel(ItemCameraTransforms cameraTransforms) {
        this.cameraTransforms = cameraTransforms;
    }

    public void clearCache() {
        Optional.ofNullable(overrideList).ifPresent(ModularOverrideList::clearCache);
    }


    @Override
    public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter,
            Set<Pair<String, String>> missingTextureErrors) {
        return Collections.emptyList();
    }

    /**
     * Fakebake, real bake is done in override
     * @param owner
     * @param bakery
     * @param spriteGetter
     * @param modelTransform
     * @param overrides
     * @param modelLocation
     * @return
     */
    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter,
            IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {

        overrideList = new ModularOverrideList(this, owner, bakery, spriteGetter, modelTransform, modelLocation);
        return new BakedWrapper(this, owner, bakery, spriteGetter, modelTransform, modelLocation, overrideList);
    }

    public IBakedModel realBake(List<ModuleModel> moduleModels, IModelConfiguration owner, ModelBakery bakery,
            Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides,
            ResourceLocation modelLocation) {
        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

        TransformationMatrix rotationTransform = modelTransform.getRotation();
        ImmutableMap<ItemCameraTransforms.TransformType, TransformationMatrix> transforms = PerspectiveMapWrapper.getTransforms(modelTransform);
        for(int i = 0; i < moduleModels.size(); i++) {
            ModuleModel model = moduleModels.get(i);
            TextureAtlasSprite tas = spriteGetter.apply(new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE, model.location));
            builder.addAll(getQuadsForSprite(i, tas, rotationTransform, model.tint));
        }
        TextureAtlasSprite particle = spriteGetter.apply(owner.resolveTexture("particle"));

        return new BakedPerspectiveModel(builder.build(), particle, transforms, overrides, rotationTransform.isIdentity(), owner.isSideLit(),
                cameraTransforms);
    }

    public static List<BakedQuad> getQuadsForSprite(int tintIndex, TextureAtlasSprite sprite, TransformationMatrix transform, int color) {
        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

        int uMax = sprite.getWidth();
        int vMax = sprite.getHeight();

        // todo: figure out why this doesn't work when considering fully transparent rows
        for (int v = 0; v < vMax; v++) {
            builder.add(buildSideQuad(transform, Direction.UP, tintIndex, color, sprite, 0, v, uMax));
            builder.add(buildSideQuad(transform, Direction.DOWN, tintIndex, color, sprite, 0, v + 1, uMax));
        }

        for (int u = 0; u < uMax; u++) {
            builder.add(buildSideQuad(transform, Direction.EAST, tintIndex, color, sprite, u + 1, 0, vMax));
            builder.add(buildSideQuad(transform, Direction.WEST, tintIndex, color, sprite, u, 0, vMax));
        }

        // front
        builder.add(buildQuad(transform, Direction.NORTH, sprite, tintIndex, color,
                0, 0, 7.5f / 16f, sprite.getMinU(), sprite.getMaxV(),
                0, 1, 7.5f / 16f, sprite.getMinU(), sprite.getMinV(),
                1, 1, 7.5f / 16f, sprite.getMaxU(), sprite.getMinV(),
                1, 0, 7.5f / 16f, sprite.getMaxU(), sprite.getMaxV()
        ));
        // back
        builder.add(buildQuad(transform, Direction.SOUTH, sprite, tintIndex, color,
                0, 0, 8.5f / 16f, sprite.getMinU(), sprite.getMaxV(),
                1, 0, 8.5f / 16f, sprite.getMaxU(), sprite.getMaxV(),
                1, 1, 8.5f / 16f, sprite.getMaxU(), sprite.getMinV(),
                0, 1, 8.5f / 16f, sprite.getMinU(), sprite.getMinV()
        ));

        return builder.build();
    }


    private static BakedQuad buildSideQuad(TransformationMatrix transform, Direction side, int tintIndex,
            int color, TextureAtlasSprite sprite, int u, int v, int size) {

        final float eps = 1e-2f;

        int width = sprite.getWidth();
        int height = sprite.getHeight();

        float x0 = (float) u / width;
        float y0 = (float) v / height;

        float x1 = x0;
        float y1 = y0;

        float z0 = 7.5f / 16f;
        float z1 = 8.5f / 16f;

        switch(side) {
            case WEST:
                z0 = 8.5f / 16f;
                z1 = 7.5f / 16f;

                y1 = (float) (v + size) / height;
                break;
            case EAST:
                y1 = (float) (v + size) / height;
                break;
            case DOWN:
                z0 = 8.5f / 16f;
                z1 = 7.5f / 16f;

                x1 = (float) (u + size) / width;
                break;
            case UP:
                x1 = (float) (u + size) / width;
                break;
            default:
                throw new IllegalArgumentException("can't handle z-oriented side");
        }

        float dx = side.getDirectionVec().getX() * eps / width;
        float dy = side.getDirectionVec().getY() * eps / height;

        float u0 = 16f * (x0 - dx);
        float u1 = 16f * (x1 - dx);
        float v0 = 16f * (1f - y0 - dy);
        float v1 = 16f * (1f - y1 - dy);

        return buildQuad(
                transform, remap(side), sprite, tintIndex, color,
                x0, y0, z0, sprite.getInterpolatedU(u0), sprite.getInterpolatedV(v0),
                x1, y1, z0, sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v1),
                x1, y1, z1, sprite.getInterpolatedU(u1), sprite.getInterpolatedV(v1),
                x0, y0, z1, sprite.getInterpolatedU(u0), sprite.getInterpolatedV(v0)
        );
    }

    private static Direction remap(Direction side) {
        // getOpposite is related to the swapping of V direction
        return side.getAxis() == Direction.Axis.Y ? side.getOpposite() : side;
    }

    private static BakedQuad buildQuad(TransformationMatrix transform, Direction side, TextureAtlasSprite sprite, int tintIndex, int color,
            float x0, float y0, float z0, float u0, float v0,
            float x1, float y1, float z1, float u1, float v1,
            float x2, float y2, float z2, float u2, float v2,
            float x3, float y3, float z3, float u3, float v3) {
        BakedQuadBuilder builder = new BakedQuadBuilder(sprite);

        builder.setQuadTint(tintIndex);
        builder.setQuadOrientation(side);

        boolean hasTransform = !transform.isIdentity();
        IVertexConsumer consumer = hasTransform ? new TRSRTransformer(builder, transform) : builder;

        putVertex(consumer, side, x0, y0, z0, u0, v0, color);
        putVertex(consumer, side, x1, y1, z1, u1, v1, color);
        putVertex(consumer, side, x2, y2, z2, u2, v2, color);
        putVertex(consumer, side, x3, y3, z3, u3, v3, color);

        return builder.build();
    }

    private static void putVertex(IVertexConsumer consumer, Direction side, float x, float y, float z, float u, float v, int color) {
        VertexFormat format = consumer.getVertexFormat();
        for(int e = 0; e < format.getElements().size(); e++) {
            switch(format.getElements().get(e).getUsage()) {
                case POSITION:
                    consumer.put(e, x, y, z, 1f);
                    break;
                case COLOR:
                    float r = ((color >> 16) & 0xFF) / 255f; // red
                    float g = ((color >>  8) & 0xFF) / 255f; // green
                    float b = ((color >>  0) & 0xFF) / 255f; // blue
                    float a = ((color >> 24) & 0xFF) / 255f; // alpha

                    // reset alpha to 1 if it's 0 to avoid mistakes & make things cleaner
                    a = a == 0 ? 1 : a;

                    consumer.put(e, r, g, b, a);
                    break;
                case NORMAL:
                    float offX = (float) side.getXOffset();
                    float offY = (float) side.getYOffset();
                    float offZ = (float) side.getZOffset();
                    consumer.put(e, offX, offY, offZ, 0f);
                    break;
                case UV:
                    if(format.getElements().get(e).getIndex() == 0)
                    {
                        consumer.put(e, u, v, 0f, 1f);
                        break;
                    }
                    // else fallthrough to default
                default:
                    consumer.put(e);
                    break;
            }
        }
    }
}