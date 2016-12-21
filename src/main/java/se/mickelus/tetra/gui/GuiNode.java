package se.mickelus.tetra.gui;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.util.ArrayList;

public class GuiNode extends Gui {

    protected ArrayList<GuiNode> elements;

    public GuiNode() {
        elements = new ArrayList<>();
    }

    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        drawChildren(refX, refY, screenWidth, screenHeight, mouseX, mouseY);
    }

    protected void drawChildren(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        elements.stream().forEach((element -> element.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY)));
    }

    public void addChild(GuiNode child) {
        this.elements.add(child);
    }

    public void clearChildren() {
        this.elements.clear();
    }

    public int getNumChildren() {
        return elements.size();
    }

    public GuiNode getChild(int index) {
        if (index >= 0 && index < elements.size()) {
            return elements.get(index);
        }
        return null;
    }


    public static void drawRect(int left, int top, int right, int bottom, int color, float opacity)
    {
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
