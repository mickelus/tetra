package se.mickelus.tetra.blocks.forged.container;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;


public class TESRForgedContainer extends TileEntitySpecialRenderer<TileEntityForgedContainer> {
    private static final ResourceLocation texture = new ResourceLocation(TetraMod.MOD_ID,"textures/blocks/forged_container/forged_container.png");
    private ModelForgedContainer model = new ModelForgedContainer();

    private static final float openDuration = 300;

    public TESRForgedContainer() {

    }

    public void render(TileEntityForgedContainer te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te.isFlipped()) {
            return;
        }
        GlStateManager.enableDepth();
        GlStateManager.depthFunc(515);
        GlStateManager.depthMask(true);

        this.bindTexture(texture);

        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();

        GlStateManager.translate((float)x, (float)y + 1.0F, (float)z + 1.0F);
        GlStateManager.scale(1.0F, -1.0F, -1.0F);
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        int j = 270;

        // todo: rotate 90 deg based on facing
        switch (te.getFacing()) {
            case NORTH:
                break;
            case WEST:
                GlStateManager.rotate(270, 0.0F, 1.0F, 0.0F);
                break;
            case SOUTH:
                GlStateManager.rotate(180, 0.0F, 1.0F, 0.0F);
                break;
            case EAST:
                GlStateManager.rotate(90, 0.0F, 1.0F, 0.0F);
                break;
        }
        GlStateManager.translate(-0.5F, -0.5F, -0.5F);

        if (te.isOpen()) {
            float progress  = Math.min(1, ( System.currentTimeMillis() - te.openTime) / openDuration);
            model.lid.rotateAngleY = (progress * 0.1f * ((float)Math.PI / 2F));
            //model.lid.offsetY = 0.5625f;
            model.lid.offsetZ = 0.3f * progress;
        } else {
            model.lid.rotateAngleY = 0;
            model.lid.offsetZ = 0;
        }
        model.render(te.isLocked());
//        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
