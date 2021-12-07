package se.mickelus.tetra.blocks.scroll;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.rack.RackBlock;
import se.mickelus.mutil.util.RotationHelper;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class ScrollRenderer implements BlockEntityRenderer<ScrollTile> {
    public static final Material material = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(TetraMod.MOD_ID,"blocks/scroll"));
    public static ModelLayerLocation layer = new ModelLayerLocation(new ResourceLocation(TetraMod.MOD_ID, "blocks/scroll"), "main");
    private final ModelPart model;

    private ModelPart[] rolledModel;
    private ModelPart ribbonModel;
    private ModelPart[] wallModel;
    private QuadRenderer[][] wallGlyphs;

    private ModelPart[] openModel;
    private QuadRenderer[][] openGlyphs;

    private static final int availableGlyphs = 16;
    private static final int availableMaterials = 3;

    ModelPart transparent;
    private BlockEntityRendererProvider.Context context;

    public ScrollRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;

        model = context.bakeLayer(layer);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();

//        rolledModel = new ModelPart[availableMaterials];
//        wallModel = new ModelPart[availableMaterials];
//        openModel = new ModelPart[availableMaterials];
//        for (int i = 0; i < availableMaterials; i++) {
//            rolledModel[i] = new ModelPart(128, 64, 34 * i, 4);
//            rolledModel[i].addBox(1, 0, 7, 14, 3, 3, 0);
//
//            wallModel[i] = new ModelPart(128, 64, 34 * i, 0);
//            wallModel[i].addBox(1, 14, 0, 14, 2, 2, 0);
//            wallModel[i].addBox("face", 1, 1, 0.05f, 14, 13, 0, 0, 34 * i, 10);
//
//            openModel[i] = new ModelPart(128, 64, 34 * i, 0);
//            openModel[i].addBox(1, 0, 0, 14, 2, 2, 0);
//            openModel[i].addBox(1, 0, 14, 14, 2, 2, 0);
//            openModel[i].addBox("face", 1, 0.05f, 2, 14, 0, 12, 0, 34 * i -12, 10);
//
//        }
//
//        ribbonModel = new ModelPart(128, 64, 0, 23);
//        ribbonModel.addBox(7, 0, 7, 2, 3, 3, 0.001f);
//
//        transparent = new ModelPart(128, 64, 0, 0);
//        transparent.addBox("face", 2, 1, 0.075f, 6, 13, 0, 0, -6, 51);
//
//        wallGlyphs = new QuadRenderer[2][];
//        for (int i = 0; i < wallGlyphs.length; i++) {
//            wallGlyphs[i] = new QuadRenderer[availableGlyphs];
//        }
//        for (int i = 0; i < availableGlyphs; i++) {
//            wallGlyphs[0][i] = new QuadRenderer(8, 1, 0.075f, 7, 13, i * 7, 51, 128, 64, true, Direction.SOUTH);
//            wallGlyphs[1][i] = new QuadRenderer(1, 1, 0.075f, 7, 13, i * 7, 51, 128, 64, false, Direction.SOUTH);
//        }
//
//        openGlyphs = new QuadRenderer[4][];
//        for (int i = 0; i < openGlyphs.length; i++) {
//            openGlyphs[i] = new QuadRenderer[availableGlyphs];
//        }
//        for (int i = 0; i < availableGlyphs; i++) {
//            openGlyphs[0][i] = new QuadRenderer(1, 0.075f, 2, 7, 6, i * 7, 58, 128, 64, true, Direction.UP);
//            openGlyphs[1][i] = new QuadRenderer(8, 0.075f, 2, 7, 6, i * 7, 58, 128, 64, false, Direction.UP);
//            openGlyphs[2][i] = new QuadRenderer(1, 0.075f, 2, 7, 6, i * 7, 58, 128, 64, true, Direction.UP);
//            openGlyphs[3][i] = new QuadRenderer(8, 0.075f, 2, 7, 6, i * 7, 58, 128, 64, false, Direction.UP);
//        }

        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void render(ScrollTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        VertexConsumer vertexBuilder = material.buffer(buffer, rl -> RenderType.entityCutout(rl));

        model.render(matrixStack, vertexBuilder, combinedLight, combinedOverlay);

//        ScrollData[] scrolls = tile.getScrolls();
//        ScrollBlock.Arrangement arrangement = ((ScrollBlock) tile.getBlockState().getBlock()).getArrangement();
//        Direction direction = tile.getBlockState().getValue(RackBlock.facingProp);
//
//        matrixStack.pushPose();
//        matrixStack.translate(0.5, 0, 0.5);
//        matrixStack.mulPose(direction.getRotation());
//        matrixStack.mulPose(Vector3f.XN.rotationDegrees(90));
//        matrixStack.translate(-0.5, 0, -0.5);
//
//
//        switch (arrangement) {
//            case rolled:
//                renderRolled(scrolls, matrixStack, combinedLight, combinedOverlay, vertexBuilder);
//                break;
//            case wall:
//                renderWall(scrolls, matrixStack, combinedLight, combinedOverlay, vertexBuilder);
//                break;
//            case open:
//                renderOpen(scrolls, matrixStack, combinedLight, combinedOverlay, vertexBuilder);
//                break;
//        }
//
//        matrixStack.popPose();
//
//        if (shouldDrawLabel(scrolls, tile.getBlockPos())) {
//            matrixStack.pushPose();
//            matrixStack.translate(0.5, 0, 0.5);
//            if (arrangement == ScrollBlock.Arrangement.wall) {
//                matrixStack.mulPose(direction.getOpposite().getRotation());
//                matrixStack.mulPose(Vector3f.XN.rotationDegrees(90));
//                matrixStack.translate(0, 0.55, 0.4);
//                drawLabel(scrolls[0], matrixStack, buffer, combinedLight);
//            } else if (arrangement == ScrollBlock.Arrangement.open) {
//                double angle = RotationHelper.getHorizontalAngle(Minecraft.getInstance().getCameraEntity().getEyePosition(partialTicks),
//                        Vec3.atCenterOf(tile.getBlockPos()));
//                Quaternion rotation = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
//                rotation.mul(Vector3f.YP.rotationDegrees((float) (angle / Math.PI * 180 + 180)));
//                matrixStack.mulPose(rotation);
//                matrixStack.translate(0, 0.4f, 0.4);
//                drawLabel(scrolls[0], matrixStack, buffer, combinedLight);
//            }
//            matrixStack.popPose();
//        }
    }

    private void renderRolled(ScrollData[] scrolls, PoseStack matrixStack, int combinedLight, int combinedOverlay, VertexConsumer vertexBuilder) {
        matrixStack.translate(0.5, 0, 0.5);
        matrixStack.mulPose(Vector3f.YN.rotationDegrees(90));
        matrixStack.translate(-0.5, 0, -0.5);
        int offset = Math.min(scrolls.length, 3) - 1;
        if (offset > 0) {
            matrixStack.translate(0, 0, offset * -0.125); // 2px
        }

        for (int i = 0; i < scrolls.length; i++) {
            int mat = getMaterial(scrolls, i);
            float red = FastColor.ARGB32.red(scrolls[i].ribbon) / 255f;
            float green = FastColor.ARGB32.green(scrolls[i].ribbon) / 255f;
            float blue = FastColor.ARGB32.blue(scrolls[i].ribbon) / 255f;

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

    private void renderWall(ScrollData[] scrolls, PoseStack matrixStack, int combinedLight, int combinedOverlay, VertexConsumer vertexBuilder) {
        int mat = getMaterial(scrolls, 0);
        int color = getGlyphColor(mat);
        float red = FastColor.ARGB32.red(color) / 255f;
        float green = FastColor.ARGB32.green(color) / 255f;
        float blue = FastColor.ARGB32.blue(color) / 255f;

        wallModel[mat].render(matrixStack, vertexBuilder, combinedLight, combinedOverlay);
        for (int i = 0; i < wallGlyphs.length; i++) {
            int glyph = getGlyph(scrolls, i);
            wallGlyphs[i][glyph].render(matrixStack, vertexBuilder, combinedLight, combinedOverlay, red, green, blue, 1);
        }
    }

    private void renderOpen(ScrollData[] scrolls, PoseStack matrixStack, int combinedLight, int combinedOverlay, VertexConsumer vertexBuilder) {
        int mat = getMaterial(scrolls, 0);
        int color = getGlyphColor(mat);
        float red = FastColor.ARGB32.red(color) / 255f;
        float green = FastColor.ARGB32.green(color) / 255f;
        float blue = FastColor.ARGB32.blue(color) / 255f;

        matrixStack.translate(0.5, 0, 0.5);
        matrixStack.mulPose(Vector3f.YN.rotationDegrees(90));
        matrixStack.translate(-0.5, 0, -0.5);

        openModel[mat].render(matrixStack, vertexBuilder, combinedLight, combinedOverlay);

        for (int i = 0; i < openGlyphs.length; i++) {
            if (i == 2) {
                matrixStack.translate(0.5, 0, 0.5);
                matrixStack.mulPose(Vector3f.YN.rotationDegrees(180));
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
                return Mth.clamp(data[0].glyphs.get(index), 0, availableGlyphs);
            }
            if (data[0].glyphs.size() > 0) {
                return Mth.clamp(data[0].glyphs.get(0), 0, availableGlyphs);
            }
        }
        return 0;
    }

    private int getMaterial(ScrollData[] data, int index) {
        if (data.length > index) {
            return Mth.clamp(data[index].material, 0, 2);
        }
        return 0;
    }

    private boolean shouldDrawLabel(ScrollData[] scrolls, BlockPos pos) {
        HitResult mouseover = Minecraft.getInstance().hitResult;
        return scrolls != null && scrolls.length > 0
                && mouseover != null && mouseover.getType() == HitResult.Type.BLOCK
                && pos.equals(((BlockHitResult) mouseover).getBlockPos());
    }

    private void drawLabel(ScrollData scroll, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        String label = I18n.get("item.tetra.scroll." + scroll.key + ".name");

        matrixStack.scale(-0.0125f, -0.0125f, 0.0125f);
        Matrix4f matrix4f = matrixStack.last().pose();
        Font fontrenderer = context.getFont();
        float x = -fontrenderer.width(label) / 2f;
        fontrenderer.drawInBatch(label, x + 1, 0, 0, false, matrix4f, buffer, false, 0, packedLight, false);
        fontrenderer.drawInBatch(label, x - 1, 0, 0, false, matrix4f, buffer, false, 0, packedLight, false);
        fontrenderer.drawInBatch(label, x, -1, 0, false, matrix4f, buffer, false, 0, packedLight, false);
        fontrenderer.drawInBatch(label, x, 1, 0, false, matrix4f, buffer, false, 0, packedLight, false);

        matrixStack.translate(0, 0, -0.0125f);
        fontrenderer.drawInBatch(label, x, 0, -1, false, matrix4f, buffer, false, 0, packedLight, false);
    }
}
