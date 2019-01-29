package se.mickelus.tetra.gui.hud;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.animation.KeyframeAnimation;

public class GuiRootHud extends GuiElement {

    public GuiRootHud() {
        super(0, 0, 0, 0);
    }

    public void draw(EntityPlayer player, BlockPos blockPos, RayTraceResult rayTrace, AxisAlignedBB boundingBox, float partialTicks) {
        double offsetX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double offsetY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double offsetZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        draw(blockPos.getX() - offsetX, blockPos.getY() - offsetY, blockPos.getZ() - offsetZ,
                rayTrace.hitVec.x - blockPos.getX(), rayTrace.hitVec.y - blockPos.getY(), rayTrace.hitVec.z - blockPos.getZ(),
                rayTrace.sideHit, boundingBox);
    }

    public void draw(double x, double y, double z, double hitX, double hitY, double hitZ, EnumFacing facing, AxisAlignedBB boundingBox) {
        activeAnimations.removeIf(keyframeAnimation -> !keyframeAnimation.isActive());
        activeAnimations.forEach(KeyframeAnimation::preDraw);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.depthMask(false);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        int mouseX = 0;
        int mouseY = 0;

        // magic number is the same used to offset the outline, stops textures from flickering
        GlStateManager.translate(facing.getDirectionVec().getX() * 0.0020000000949949026D, facing.getDirectionVec().getY() * 0.0020000000949949026D, facing.getDirectionVec().getZ() * 0.0020000000949949026D);

        switch (facing) {
            case NORTH:
                mouseY = (int) ( ( 1 - hitY ) * 32 );
                mouseX = (int) ( ( 1 - hitX ) * 32 );

                GlStateManager.translate(1, 1, boundingBox.minZ);
                GlStateManager.rotate(180, 0, 1, 0);
                break;
            case SOUTH:
                mouseY = (int) ( ( 1 - hitY ) * 32 );
                mouseX = (int) ( hitX * 32 );

                GlStateManager.translate(0, 1, boundingBox.maxZ);
                break;
            case EAST:
                mouseY = (int) ( ( 1 - hitY ) * 32 );
                mouseX = (int) ( ( 1 - hitZ ) * 32 );

                GlStateManager.translate(boundingBox.maxX, 1, 1);
                GlStateManager.rotate(90, 0, 1, 0);
                break;
            case WEST:
                mouseY = (int) ( ( 1 - hitY ) * 32 );
                mouseX = (int) ( hitZ * 32 );

                GlStateManager.translate(boundingBox.minX, 1, 0);
                GlStateManager.rotate(-90, 0, 1, 0);
                break;
            case UP:
                GlStateManager.translate(1, boundingBox.maxY, 1);
                GlStateManager.rotate(90, 1, 0, 0);
                GlStateManager.scale(-1, 1, 1);
                break;
            case DOWN:
                GlStateManager.translate(0, boundingBox.minY, 1);
                GlStateManager.rotate(90, 1, 0, 0);
                break;
        }

        // 0.03125 = 1/32
        GlStateManager.scale(0.03125, -0.03125, 0.03125);
        drawChildren(0, 0, 32, 32, mouseX, mouseY, 1);
        GlStateManager.popMatrix();

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}
