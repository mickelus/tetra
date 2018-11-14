package se.mickelus.tetra.gui.hud;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.animation.KeyframeAnimation;

public class GuiRootHud extends GuiElement {

    public GuiRootHud() {
        super(0, 0, 0, 0);
    }

    public void draw(EntityPlayer player, BlockPos blockPos, EnumFacing facing, float partialTicks) {
        double offsetX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double offsetY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double offsetZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
        draw(blockPos.getX() - offsetX, blockPos.getY() - offsetY, blockPos.getZ() - offsetZ, facing);
    }

    public void draw(double x, double y, double z, EnumFacing facing) {
        activeAnimations.removeIf(keyframeAnimation -> !keyframeAnimation.isActive());
        activeAnimations.forEach(KeyframeAnimation::preDraw);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.depthMask(false);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        // magic number is the same used to offset the outline, stops textures from flickering
        GlStateManager.translate(facing.getDirectionVec().getX() * 0.0020000000949949026D, facing.getDirectionVec().getY() * 0.0020000000949949026D, facing.getDirectionVec().getZ() * 0.0020000000949949026D);

        switch (facing) {
            case NORTH:
                GlStateManager.translate(1, 1, 0);
                GlStateManager.rotate(180, 0, 1, 0);
                break;
            case EAST:
                GlStateManager.translate(1, 1, 1);
                GlStateManager.rotate(90, 0, 1, 0);
                break;
            case SOUTH:
                GlStateManager.translate(0, 1, 1);
                break;
            case WEST:
                GlStateManager.translate(0, 1, 0);
                GlStateManager.rotate(-90, 0, 1, 0);
                break;
            case UP:
                GlStateManager.translate(1, 1, 1);
                GlStateManager.rotate(90, 1, 0, 0);
                GlStateManager.scale(-1, 1, 1);
                break;
            case DOWN:
                GlStateManager.translate(0, 0, 1);
                GlStateManager.rotate(90, 1, 0, 0);
                break;
        }

        // 0.0625 = 1/16
        GlStateManager.scale(0.0625, -0.0625, 0.0625);
        drawChildren(0, 0, 16, 16, 0, 0, 1);
        GlStateManager.popMatrix();

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}
