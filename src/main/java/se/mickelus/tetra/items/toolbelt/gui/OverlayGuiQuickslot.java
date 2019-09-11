package se.mickelus.tetra.items.toolbelt.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.impl.GuiColors;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.animation.Applier;
import se.mickelus.tetra.gui.animation.KeyframeAnimation;

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

        mc = Minecraft.getMinecraft();

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
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        super.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);

        if (hasFocus()) {
            fontRenderer.drawStringWithShadow(itemStack.getDisplayName(), x + refX + 63, y + refY + 4, GuiColors.hover);
        }

        drawItemStack(itemStack, x + refX + 38, y + refY + 1);
    }

    private void drawItemStack(ItemStack itemStack, int x, int y) {
        GlStateManager.pushMatrix();
        GlStateManager.enableDepth();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderHelper.enableGUIStandardItemLighting();

        mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, x, y);
        mc.getRenderItem().renderItemOverlayIntoGUI(fontRenderer, itemStack, x, y, null);
        if (opacity < 1) {
            this.zLevel = 300;
            GlStateManager.disableDepth();
            drawRect(x - 1, y - 1, x + 17, y + 17, 0, 1 - opacity);
        }
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
    }


    public int getSlot() {
        return slot;
    }
}
