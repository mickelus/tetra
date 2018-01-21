package se.mickelus.tetra.items.toolbelt.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.animation.Applier;
import se.mickelus.tetra.gui.animation.KeyframeAnimation;

public class OverlayGuiItem extends GuiElement {

    private ItemStack itemStack;

    private int slot;

    private Minecraft mc;

    private static final int COLOR_DEFAULT = 0xffffffff;
    private static final int COLOR_HOVER = 0xffffff00;

    private KeyframeAnimation showAnimation;

    public OverlayGuiItem(int x, int y, ItemStack itemStack, int slot) {
        super(x, y, 200, 24);

        this.itemStack = itemStack;
        this.slot = slot;

        mc = Minecraft.getMinecraft();

        isVisible = false;
        opacity = 0;
        showAnimation = new KeyframeAnimation(50, this)
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
            mc.fontRenderer.drawStringWithShadow(itemStack.getDisplayName(), x + refX + 63, y + refY + 4, COLOR_HOVER);
        }

        drawItemStack(itemStack, x + refX + 38, y + refY, null);
    }

    private void drawItemStack(ItemStack stack, int x, int y, String altText) {
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderHelper.enableGUIStandardItemLighting();

        // GlStateManager.translate(0.0F, 0.0F, 32.0F);
        this.zLevel = 200.0F;
        mc.getRenderItem().zLevel = 200.0F;
        FontRenderer font = null;

        if (stack != null) {
            font = stack.getItem().getFontRenderer(stack);
        }

        if (font == null) {
            font = mc.fontRenderer;
        }

        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
        mc.getRenderItem().renderItemOverlayIntoGUI(font, stack, x, y, altText);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        drawRect(x, y, x + 16, y + 16, 0, 1 - opacity);
        this.zLevel = 0.0F;
        mc.getRenderItem().zLevel = 0.0F;
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
    }


    public int getSlot() {
        return slot;
    }
}
