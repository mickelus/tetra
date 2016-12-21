package se.mickelus.tetra.gui.hud;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;

public abstract class TileEntityHudRenderer<T extends TileEntity> extends TileEntitySpecialRenderer<T> {

    protected void renderHud(String str) {
        renderString(str);

        GlStateManager.pushMatrix();
        GlStateManager.translate(1, 0.5 ,1);

        GlStateManager.scale(1/16f, -1/16f, 1/16f);

//        GlStateManager.translate(0.0F, 0.33333334F, 0.046666667F);
//        GlStateManager.scale(0.010416667F, -0.010416667F, 0.010416667F);
//        GlStateManager.scale(0.5, 0.5, 0.5);
        GlStateManager.glNormal3f(0.0F, 0.0F, -0.010416667F);

        GlStateManager.disableLighting();
        drawRect(0, 0, 10, 1, 0xffffffff, 1);
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    protected void renderString(String str) {
        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.translate(0.7f, 1.2f, 0.5f);

        GlStateManager.translate(0.0F, 0.33333334F, 0.046666667F);
        GlStateManager.scale(0.010416667F, -0.010416667F, 0.010416667F);
        GlStateManager.scale(0.5, 0.5, 0.5);
        GlStateManager.glNormal3f(0.0F, 0.0F, -0.010416667F);
        GlStateManager.depthMask(false);
        GlStateManager.disableLighting();
        getFontRenderer().drawString(str, 0, 0, 0xffffffff, true);

        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }

    public static void drawRect(int left, int top, int right, int bottom, int color, float opacity) {
        if (left < right) {
            int i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            int j = top;
            top = bottom;
            bottom = j;
        }

        float red = (float)(color >> 16 & 255) / 255.0F;
        float blue = (float)(color >> 8 & 255) / 255.0F;
        float green = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(red, blue, green, opacity);
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION);
        vertexbuffer.pos((double)left, (double)bottom, 0.0D).endVertex();
        vertexbuffer.pos((double)right, (double)bottom, 0.0D).endVertex();
        vertexbuffer.pos((double)right, (double)top, 0.0D).endVertex();
        vertexbuffer.pos((double)left, (double)top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}
