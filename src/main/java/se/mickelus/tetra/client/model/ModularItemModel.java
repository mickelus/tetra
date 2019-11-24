package se.mickelus.tetra.client.model;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.ISprite;
import net.minecraftforge.client.model.BakedItemModel;
import net.minecraftforge.client.model.PerspectiveMapWrapper;

import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.TRSRTransformer;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.TRSRTransformation;

import java.util.*;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Forge reimplementation of vanilla {@link ItemModelGenerator}, i.e. builtin/generated models,
 * with the following changes:
 * - Represented as a true {@link IUnbakedModel} so it can be baked as usual instead of using
 *   special-case logic like vanilla does.
 * - Various fixes in the baking logic.
 * - Not limited to 4 layers maximum.
 */
public final class ModularItemModel implements IUnbakedModel {
    private static final Direction[] HORIZONTALS = {Direction.UP, Direction.DOWN};
    private static final Direction[] VERTICALS = {Direction.WEST, Direction.EAST};

    private final ImmutableList<ResourceLocation> textures;

    public ModularItemModel(ImmutableList<ResourceLocation> textures) {
        this.textures = textures;
    }

    private static ImmutableList<ResourceLocation> getTextures(BlockModel model)
    {
        ImmutableList.Builder<ResourceLocation> builder = ImmutableList.builder();
        for(int i = 0; model.isTexturePresent("layer" + i); i++)
        {
            builder.add(new ResourceLocation(model.resolveTextureName("layer" + i)));
        }
        return builder.build();
    }

    @Override
    public Collection<ResourceLocation> getTextures(Function<ResourceLocation, IUnbakedModel> modelGetter, Set<String> missingTextureErrors) {
        return textures;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public ModularItemModel retexture(ImmutableMap<String, String> textures)
    {
        ImmutableList.Builder<ResourceLocation> builder = ImmutableList.builder();
        for(int i = 0; i < textures.size() + this.textures.size(); i++)
        {
            if(textures.containsKey("layer" + i))
            {
                builder.add(new ResourceLocation(textures.get("layer" + i)));
            }
            else if(i < this.textures.size())
            {
                builder.add(this.textures.get(i));
            }
        }
        return new ModularItemModel(builder.build());
    }

    @Nullable
    @Override
    public IBakedModel bake(ModelBakery bakery, Function<ResourceLocation, TextureAtlasSprite> spriteGetter, ISprite sprite, VertexFormat format) {
        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

        Optional<TRSRTransformation> transform = sprite.getState().apply(Optional.empty());
        ImmutableMap<TransformType, TRSRTransformation> map = PerspectiveMapWrapper.getTransforms(sprite.getState());
        boolean identity = !transform.isPresent() || transform.get().isIdentity();

        for(int i = 0; i < textures.size(); i++) {
            TextureAtlasSprite tas = spriteGetter.apply(textures.get(i));
            builder.addAll(getQuadsForSprite(i, tas, format, transform));
        }
        TextureAtlasSprite particle = spriteGetter.apply(textures.isEmpty() ? new ResourceLocation("missingno") : textures.get(0));

        return new BakedItemModel(builder.build(), particle, map, ItemOverrideList.EMPTY, identity);
    }

    public static List<BakedQuad> getQuadsForSprite(int tint, TextureAtlasSprite sprite, VertexFormat format, Optional<TRSRTransformation> transform) {
        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

        int uMax = sprite.getWidth();
        int vMax = sprite.getHeight();

        // todo 1.14: figure out why this doesn't work when considering fully transparent rows
        for (int v = 0; v < vMax; v++) {
            builder.add(buildSideQuad(format, transform, Direction.UP, tint, sprite, 0, v, uMax));
            builder.add(buildSideQuad(format, transform, Direction.DOWN, tint, sprite, 0, v + 1, uMax));
        }

        for (int u = 0; u < uMax; u++) {
            builder.add(buildSideQuad(format, transform, Direction.EAST, tint, sprite, u + 1, 0, vMax));
            builder.add(buildSideQuad(format, transform, Direction.WEST, tint, sprite, u, 0, vMax));
        }

        // front
        builder.add(buildQuad(format, transform, Direction.NORTH, sprite, tint, 0xffffffff,
                0, 0, 7.5f / 16f, sprite.getMinU(), sprite.getMaxV(),
                0, 1, 7.5f / 16f, sprite.getMinU(), sprite.getMinV(),
                1, 1, 7.5f / 16f, sprite.getMaxU(), sprite.getMinV(),
                1, 0, 7.5f / 16f, sprite.getMaxU(), sprite.getMaxV()
        ));
        // back
        builder.add(buildQuad(format, transform, Direction.SOUTH, sprite, tint, 0xffffffff,
                0, 0, 8.5f / 16f, sprite.getMinU(), sprite.getMaxV(),
                1, 0, 8.5f / 16f, sprite.getMaxU(), sprite.getMaxV(),
                1, 1, 8.5f / 16f, sprite.getMaxU(), sprite.getMinV(),
                0, 1, 8.5f / 16f, sprite.getMinU(), sprite.getMinV()
        ));

        return builder.build();
    }


    private static BakedQuad buildSideQuad(VertexFormat format, Optional<TRSRTransformation> transform, Direction side, int tintIndex,
            TextureAtlasSprite sprite, int u, int v, int size) {
        int color = 0xffffffff;

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
                format, transform, remap(side), sprite, tintIndex, color,
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

    private static BakedQuad buildQuad(VertexFormat format, Optional<TRSRTransformation> transform, Direction side,
            TextureAtlasSprite sprite, int tintIndex, int color,
            float x0, float y0, float z0, float u0, float v0,
            float x1, float y1, float z1, float u1, float v1,
            float x2, float y2, float z2, float u2, float v2,
            float x3, float y3, float z3, float u3, float v3) {
        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);

        builder.setQuadTint(tintIndex);
        builder.setQuadOrientation(side);
        builder.setTexture(sprite);

        boolean hasTransform = transform.isPresent() && !transform.get().isIdentity();
        IVertexConsumer consumer = hasTransform ? new TRSRTransformer(builder, transform.get()) : builder;

        putVertex(consumer, format, side, x0, y0, z0, u0, v0, color);
        putVertex(consumer, format, side, x1, y1, z1, u1, v1, color);
        putVertex(consumer, format, side, x2, y2, z2, u2, v2, color);
        putVertex(consumer, format, side, x3, y3, z3, u3, v3, color);

        return builder.build();
    }

    private static void putVertex(IVertexConsumer consumer, VertexFormat format, Direction side, float x, float y, float z, float u,
            float v, int color) {
        for(int e = 0; e < format.getElementCount(); e++) {
            switch(format.getElement(e).getUsage()) {
                case POSITION:
                    consumer.put(e, x, y, z, 1f);
                    break;
                case COLOR:
                    float r = ((color >> 16) & 0xFF) / 255f; // red
                    float g = ((color >>  8) & 0xFF) / 255f; // green
                    float b = ((color >>  0) & 0xFF) / 255f; // blue
                    float a = ((color >> 24) & 0xFF) / 255f; // alpha
                    consumer.put(e, r, g, b, a);
                    break;
                case NORMAL:
                    float offX = (float) side.getXOffset();
                    float offY = (float) side.getYOffset();
                    float offZ = (float) side.getZOffset();
                    consumer.put(e, offX, offY, offZ, 0f);
                    break;
                case UV:
                    if(format.getElement(e).getIndex() == 0) {
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