package se.mickelus.tetra.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiString;

public class GuiStringSmall2 extends GuiString {
    MutableComponent textComponent;

    public GuiStringSmall2(int x, int y, String string) {
        super(x, y, string);
        setString(string);
    }

    public GuiStringSmall2(int x, int y, String string, int color) {
        super(x, y, string, color);
        setString(string);
    }

    public GuiStringSmall2(int x, int y, String string, GuiAttachment attachment) {
        super(x, y, string, attachment);
        setString(string);
    }

    public GuiStringSmall2(int x, int y, String string, int color, GuiAttachment attachment) {
        super(x, y, string, color, attachment);
        setString(string);
    }

    public void setString(String string) {
        if (string != null) {
            textComponent = Component.literal(string.toUpperCase()).withStyle(Style.EMPTY.withFont(new ResourceLocation("tetra", "ascii_small")));
            this.width = this.fontRenderer.width(textComponent);
            if (this.fixedWidth) {
                textComponent = (MutableComponent) fontRenderer.substrByWidth(textComponent, width);
            } else {
                this.width = this.fontRenderer.width(textComponent);
            }
        }

    }

    protected void drawString(PoseStack matrixStack, String text, int x, int y, int color, float opacity, boolean drawShadow) {
        color = colorWithOpacity(color, opacity);
        if ((color & -67108864) != 0) {
            matrixStack.pushPose();
            MultiBufferSource.BufferSource renderTypeBuffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            this.fontRenderer.drawInBatch(textComponent, (float) x, (float) y, color, drawShadow, matrixStack.last().pose(), renderTypeBuffer, true, 0, 15728880);
            renderTypeBuffer.endBatch();
            matrixStack.popPose();
        }

    }
}
