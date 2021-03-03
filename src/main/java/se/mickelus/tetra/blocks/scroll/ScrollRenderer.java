package se.mickelus.tetra.blocks.scroll;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.rack.RackBlock;

public class ScrollRenderer extends TileEntityRenderer<ScrollTile> {
    public static final RenderMaterial material = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation(TetraMod.MOD_ID,"blocks/scroll"));

    private ModelRenderer[] rolledModel;
    private ModelRenderer ribbonModel;
    private ModelRenderer[] wallModel;
    private QuadRenderer[][] wallGlyphs;

    private ModelRenderer[] openModel;
    private QuadRenderer[][] openGlyphs;

    private static final int availableGlyphs = 16;
    private static final int availableMaterials = 3;

    ModelRenderer transparent;

    public ScrollRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);

        rolledModel = new ModelRenderer[availableMaterials];
        wallModel = new ModelRenderer[availableMaterials];
        openModel = new ModelRenderer[availableMaterials];
        for (int i = 0; i < availableMaterials; i++) {
            rolledModel[i] = new ModelRenderer(128, 64, 34 * i, 4);
            rolledModel[i].addBox(1, 0, 7, 14, 3, 3, 0);

            wallModel[i] = new ModelRenderer(128, 64, 34 * i, 0);
            wallModel[i].addBox(1, 14, 0, 14, 2, 2, 0);
            wallModel[i].addBox("face", 1, 1, 0.05f, 14, 13, 0, 0, 34 * i, 10);

            openModel[i] = new ModelRenderer(128, 64, 34 * i, 0);
            openModel[i].addBox(1, 0, 0, 14, 2, 2, 0);
            openModel[i].addBox(1, 0, 14, 14, 2, 2, 0);
            openModel[i].addBox("face", 1, 0.05f, 2, 14, 0, 12, 0, 34 * i -12, 10);

        }

        ribbonModel = new ModelRenderer(128, 64, 0, 23);
        ribbonModel.addBox(7, 0, 7, 2, 3, 3, 0.001f);

        transparent = new ModelRenderer(128, 64, 0, 0);
        transparent.addBox("face", 2, 1, 0.075f, 6, 13, 0, 0, -6, 51);
//        transparent.addBox("face", 2, 1, 0.1f, 6, 13, 0, 0, 20, 32);

        wallGlyphs = new QuadRenderer[2][];
        for (int i = 0; i < wallGlyphs.length; i++) {
            wallGlyphs[i] = new QuadRenderer[availableGlyphs];
        }
        for (int i = 0; i < availableGlyphs; i++) {
            wallGlyphs[0][i] = new QuadRenderer(8, 1, 0.075f, 7, 13, i * 7, 51, 128, 64, true, Direction.SOUTH);
            wallGlyphs[1][i] = new QuadRenderer(1, 1, 0.075f, 7, 13, i * 7, 51, 128, 64, false, Direction.SOUTH);
        }

        openGlyphs = new QuadRenderer[4][];
        for (int i = 0; i < openGlyphs.length; i++) {
            openGlyphs[i] = new QuadRenderer[availableGlyphs];
        }
        for (int i = 0; i < availableGlyphs; i++) {
            openGlyphs[0][i] = new QuadRenderer(1, 0.075f, 2, 7, 6, i * 7, 58, 128, 64, true, Direction.UP);
            openGlyphs[1][i] = new QuadRenderer(8, 0.075f, 2, 7, 6, i * 7, 58, 128, 64, false, Direction.UP);
            openGlyphs[2][i] = new QuadRenderer(1, 0.075f, 2, 7, 6, i * 7, 58, 128, 64, true, Direction.UP);
            openGlyphs[3][i] = new QuadRenderer(8, 0.075f, 2, 7, 6, i * 7, 58, 128, 64, false, Direction.UP);
        }
    }

    @Override
    public void render(ScrollTile tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        IVertexBuilder vertexBuilder = material.getBuffer(buffer, RenderType::getEntityCutout);

        Direction direction = tile.getBlockState().get(RackBlock.facingProp);
        matrixStack.push();
        matrixStack.translate(0.5, 0, 0.5);
        matrixStack.rotate(direction.getRotation());
        matrixStack.rotate(Vector3f.XN.rotationDegrees(90));
        matrixStack.translate(-0.5, 0, -0.5);

        ScrollData[] scrolls = tile.getScrolls();

        switch (((ScrollBlock) tile.getBlockState().getBlock()).getArrangement()) {
            case rolled:
                renderRolled(scrolls, matrixStack, combinedLight, combinedOverlay, vertexBuilder);
                break;
            case wall:
                renderWall(scrolls, matrixStack, combinedLight, combinedOverlay, vertexBuilder);
                break;
            case open:
                renderOpen(scrolls, matrixStack, combinedLight, combinedOverlay, vertexBuilder);
                break;
        }

        matrixStack.pop();
    }

    private void renderRolled(ScrollData[] scrolls, MatrixStack matrixStack, int combinedLight, int combinedOverlay, IVertexBuilder vertexBuilder) {
        matrixStack.translate(0.5, 0, 0.5);
        matrixStack.rotate(Vector3f.YN.rotationDegrees(90));
        matrixStack.translate(-0.5, 0, -0.5);
        int offset = Math.min(scrolls.length, 3) - 1;
        if (offset > 0) {
            matrixStack.translate(0, 0, offset * -0.125); // 2px
        }

        for (int i = 0; i < scrolls.length; i++) {
            int mat = getMaterial(scrolls, i);
            float red = ColorHelper.PackedColor.getRed(scrolls[i].ribbon) / 255f;
            float green = ColorHelper.PackedColor.getGreen(scrolls[i].ribbon) / 255f;
            float blue = ColorHelper.PackedColor.getBlue(scrolls[i].ribbon) / 255f;

            rolledModel[mat].render(matrixStack, vertexBuilder, combinedLight, combinedOverlay);
            ribbonModel.render(matrixStack, vertexBuilder, combinedLight, combinedOverlay, red, green, blue, 1);

            matrixStack.translate(0, 0, 0.25f); // 4px

            if (i == 2) {
                matrixStack.translate(0, 0.1875, -0.625); // 10px
            } else if (i == 4) {
                matrixStack.translate(0, 0.1875, -0.375); // 6px
            }
        }
    }

    private void renderWall(ScrollData[] scrolls, MatrixStack matrixStack, int combinedLight, int combinedOverlay, IVertexBuilder vertexBuilder) {
        int mat = getMaterial(scrolls, 0);
        int color = getGlyphColor(mat);
        float red = ColorHelper.PackedColor.getRed(color) / 255f;
        float green = ColorHelper.PackedColor.getGreen(color) / 255f;
        float blue = ColorHelper.PackedColor.getBlue(color) / 255f;


        wallModel[mat].render(matrixStack, vertexBuilder, combinedLight, combinedOverlay);
        for (int i = 0; i < wallGlyphs.length; i++) {
            int glyph = getGlyph(scrolls, i);
            wallGlyphs[i][glyph].render(matrixStack, vertexBuilder, combinedLight, combinedOverlay, red, green, blue, 1);
        }
    }

    private void renderOpen(ScrollData[] scrolls, MatrixStack matrixStack, int combinedLight, int combinedOverlay, IVertexBuilder vertexBuilder) {
        int mat = getMaterial(scrolls, 0);
        int color = getGlyphColor(mat);
        float red = ColorHelper.PackedColor.getRed(color) / 255f;
        float green = ColorHelper.PackedColor.getGreen(color) / 255f;
        float blue = ColorHelper.PackedColor.getBlue(color) / 255f;

        matrixStack.translate(0.5, 0, 0.5);
        matrixStack.rotate(Vector3f.YN.rotationDegrees(90));
        matrixStack.translate(-0.5, 0, -0.5);

        openModel[mat].render(matrixStack, vertexBuilder, combinedLight, combinedOverlay);

        for (int i = 0; i < openGlyphs.length; i++) {
            if (i == 2) {
                matrixStack.translate(0.5, 0, 0.5);
                matrixStack.rotate(Vector3f.YN.rotationDegrees(180));
                matrixStack.translate(-0.5, 0, -0.5);
            }

            int glyph = getGlyph(scrolls, i);
            openGlyphs[i][glyph].render(matrixStack, vertexBuilder, combinedLight, combinedOverlay, red, green, blue, 1);
        }
    }

    private int getGlyphColor(int material) {
        switch (material) {
            case 2:
                return 0xbfa12a;
            case 1:
                return 0x8f9bcc;
            default:
            case 0:
                return 0x665f47;
        }
    }

    private int getGlyph(ScrollData[] data, int index) {
        if (data.length > 0) {
            if (data[0].glyphs.size() > index) {
                return MathHelper.clamp(data[0].glyphs.get(index), 0, availableGlyphs);
            }
            if (data[0].glyphs.size() > 0) {
                return MathHelper.clamp(data[0].glyphs.get(0), 0, availableGlyphs);
            }
        }
        return 0;
    }

    private int getMaterial(ScrollData[] data, int index) {
        if (data.length > index) {
            return MathHelper.clamp(data[index].material, 0, 2);
        }
        return 0;
    }
}
