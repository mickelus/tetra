package se.mickelus.tetra.blocks.forged.container;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.TetraMod;

// todo 1.15: ripped out
@OnlyIn(Dist.CLIENT)
public class ForgedContainerTESR extends TileEntityRenderer<ForgedContainerTile> {
    private static final ResourceLocation texture = new ResourceLocation(TetraMod.MOD_ID,"textures/blocks/forged_container/forged_container.png");
//    private ForgedContainerModel model = new ForgedContainerModel();

    private static final float openDuration = 300;

    public ForgedContainerTESR(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(ForgedContainerTile te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int i, int i1) {
        if (te.isFlipped()) {
            return;
        }

        if (te.hasWorld()) {
            BlockPos pos = te.getPos();
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(515);
            RenderSystem.depthMask(true);

//            this.bindTexture(texture);

            RenderSystem.pushMatrix();
            RenderSystem.enableRescaleNormal();

            RenderSystem.translated(pos.getX(), pos.getY() + 1, pos.getZ() + 1);
            RenderSystem.scalef(1.0F, -1.0F, -1.0F);
            RenderSystem.translatef(0.5F, 0.5F, 0.5F);
            int j = 270;

            // todo: rotate 90 deg based on facing
            switch (te.getFacing()) {
                case NORTH:
                    break;
                case WEST:
                    RenderSystem.rotatef(270, 0.0F, 1.0F, 0.0F);
                    break;
                case SOUTH:
                    RenderSystem.rotatef(180, 0.0F, 1.0F, 0.0F);
                    break;
                case EAST:
                    RenderSystem.rotatef(90, 0.0F, 1.0F, 0.0F);
                    break;
            }
            RenderSystem.translatef(-0.5F, -0.5F, -0.5F);

//            if (te.isOpen()) {
//                float progress = Math.min(1, (System.currentTimeMillis() - te.openTime) / openDuration);
//                model.lid.rotateAngleY = (progress * 0.1f * ((float) Math.PI / 2F));
//                model.lid.offsetZ = 0.3f * progress;
//            } else {
//                model.lid.rotateAngleY = 0;
//                model.lid.offsetZ = 0;
//            }
//            model.render(te.isLocked());
            RenderSystem.popMatrix();
        }

    }
}
