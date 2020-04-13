package se.mickelus.tetra.items.modular.impl.toolbelt.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;

public class OverlayGuiQuickslot extends GuiElement {

    public static final int height = 20;

    private ItemStack itemStack;

    private int slot;

    private Minecraft mc;

    private KeyframeAnimation showAnimation;

    private FontRenderer fontRenderer;

    public OverlayGuiQuickslot(int x, int y, ItemStack itemStack, int slot) {
        super(x, y, 200, height);

        this.itemStack = itemStack;
        this.slot = slot;

        mc = Minecraft.getInstance();

        if (itemStack != null) {
            fontRenderer = itemStack.getItem().getFontRenderer(itemStack);
        }

        if (fontRenderer == null) {
            fontRenderer = mc.fontRenderer;
        }

        isVisible = false;
        opacity = 0;
        showAnimation = new KeyframeAnimation(80, this)
            .applyTo(new Applier.Opacity(1), new Applier.TranslateY(y - 3, y))
            .withDelay(slot * 100);

    }

    @Override
    protected void onShow() {
        showAnimation.start();
    }

    @Override
    protected boolean onHide() {
        if (showAnimation.isActive()) {
            showAnimation.stop();
        }
        opacity = 0;
        return true;
    }

    @Override
    public void draw(MatrixStack matrixStack, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        super.draw(matrixStack, refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);

        if (hasFocus()) {
            fontRenderer.drawStringWithShadow(itemStack.getDisplayName().getUnformattedComponentText(), x + refX + 63, y + refY + 4, GuiColors.hover);
        }

        drawItemStack(matrixStack, itemStack, x + refX + 38, y + refY + 1);
    }

    private void drawItemStack(MatrixStack matrixStack, ItemStack itemStack, int x, int y) {
        GlStateManager.enableDepthTest();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderHelper.enableStandardItemLighting();

        RenderSystem.color4f(1, 1, 1, opacity);
        mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, x, y);
        mc.getItemRenderer().renderItemOverlayIntoGUI(fontRenderer, itemStack, x, y, null);
        if (opacity < 1) {
            GlStateManager.disableDepthTest();
//            drawRect(matrixStack, x - 1, y - 1, x + 17, y + 17, 0, 1 - opacity);
        }
        RenderSystem.color4f(1, 1, 1, 1);
        RenderHelper.disableStandardItemLighting();
    }


    public int getSlot() {
        return slot;
    }
}
