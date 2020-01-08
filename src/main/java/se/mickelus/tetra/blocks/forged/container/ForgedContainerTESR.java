package se.mickelus.tetra.blocks.forged.container;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;


public class ForgedContainerTESR extends TileEntityRenderer<ForgedContainerTile> {
    private static final ResourceLocation texture = new ResourceLocation(TetraMod.MOD_ID,"textures/blocks/forged_container/forged_container.png");
    private ForgedContainerModel model = new ForgedContainerModel();

    private static final float openDuration = 300;

    public ForgedContainerTESR() { }

    @Override
    public void render(ForgedContainerTile te, double x, double y, double z, float partialTicks, int destroyStage) {
        if (te.isFlipped()) {
            return;
        }

        if (destroyStage >= 0) {
            bindTexture(DESTROY_STAGES[destroyStage]);
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.scalef(4.0F, 4.0F, 1.0F);
            GlStateManager.translatef(0.0625F, 0.0625F, 0.0625F);
            GlStateManager.matrixMode(5888);
        } else {
            bindTexture(texture);
        }

        if (te.hasWorld()) {
            GlStateManager.enableDepthTest();
            GlStateManager.depthFunc(515);
            GlStateManager.depthMask(true);

            this.bindTexture(texture);

            GlStateManager.pushMatrix();
            GlStateManager.enableRescaleNormal();

            GlStateManager.translated(x, y + 1, z + 1);
            GlStateManager.scalef(1.0F, -1.0F, -1.0F);
            GlStateManager.translatef(0.5F, 0.5F, 0.5F);
            int j = 270;

            // todo: rotate 90 deg based on facing
            switch (te.getFacing()) {
                case NORTH:
                    break;
                case WEST:
                    GlStateManager.rotatef(270, 0.0F, 1.0F, 0.0F);
                    break;
                case SOUTH:
                    GlStateManager.rotatef(180, 0.0F, 1.0F, 0.0F);
                    break;
                case EAST:
                    GlStateManager.rotatef(90, 0.0F, 1.0F, 0.0F);
                    break;
            }
            GlStateManager.translatef(-0.5F, -0.5F, -0.5F);

            if (te.isOpen()) {
                float progress = Math.min(1, (System.currentTimeMillis() - te.openTime) / openDuration);
                model.lid.rotateAngleY = (progress * 0.1f * ((float) Math.PI / 2F));
                //model.lid.offsetY = 0.5625f;
                model.lid.offsetZ = 0.3f * progress;
            } else {
                model.lid.rotateAngleY = 0;
                model.lid.offsetZ = 0;
            }
            model.render(te.isLocked());
//        GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
//            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        if (destroyStage >= 0) {
            GlStateManager.matrixMode(5890);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
        }
    }
}
