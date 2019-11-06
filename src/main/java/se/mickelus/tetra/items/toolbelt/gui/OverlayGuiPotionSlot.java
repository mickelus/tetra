package se.mickelus.tetra.items.toolbelt.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;

public class OverlayGuiPotionSlot extends GuiElement {

    private static final ResourceLocation texture = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/toolbelt-inventory.png");

    private ItemStack itemStack;

    private int slot;

    private Minecraft mc;

    private KeyframeAnimation showAnimation;

    private FontRenderer fontRenderer;

    GuiTexture backdrop;

    public OverlayGuiPotionSlot(int x, int y, ItemStack itemStack, int slot, boolean animateUp) {
        super(x, y, 23, 23);

        setAttachmentPoint(GuiAttachment.middleLeft);
        setAttachmentAnchor(GuiAttachment.middleLeft);

        this.itemStack = itemStack;
        this.slot = slot;

        mc = Minecraft.getInstance();

        if (itemStack != null) {
            fontRenderer = itemStack.getItem().getFontRenderer(itemStack);
        }

        if (fontRenderer == null) {
            fontRenderer = mc.fontRenderer;
        }

        backdrop  = new GuiTexture(0, 0, 23, 23, 32, 28, texture);
        addChild(backdrop);

        isVisible = false;
        showAnimation = new KeyframeAnimation(80, this)
            .applyTo(new Applier.TranslateY(animateUp ? y + 2 : y - 2, y), new Applier.Opacity(0, 1))
            .withDelay((int) (Math.random() * 300));
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
        return true;
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        super.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);

        if (this.opacity == 1) {
            drawItemStack(itemStack, x + refX + 3, y + refY + 2);
        }
    }

    private void drawItemStack(ItemStack itemStack, int x, int y) {
        GlStateManager.pushMatrix();
        GlStateManager.enableDepthTest();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderHelper.enableGUIStandardItemLighting();

        mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, x, y);
        mc.getItemRenderer().renderItemOverlayIntoGUI(fontRenderer, itemStack, x, y, "");
        GlStateManager.disableDepthTest();

        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
    }


    public int getSlot() {
        return slot;
    }

    @Override
    protected void onFocus() {
        backdrop.setColor(GuiColors.hover);
    }

    @Override
    protected void onBlur() {
        backdrop.setColor(GuiColors.normal);
    }

    @Override
    protected void calculateFocusState(int refX, int refY, int mouseX, int mouseY) {
        mouseX -= refX + x;
        mouseY -= refY + y;
        boolean gainFocus = true;

        if (mouseX + mouseY < 12) {
            gainFocus = false;
        }

        if (mouseX + mouseY > 34) {
            gainFocus = false;
        }

        if (mouseX - mouseY > 8) {
            gainFocus = false;
        }

        if (mouseY - mouseX > 12) {
            gainFocus = false;
        }

        if (gainFocus != hasFocus) {
            hasFocus = gainFocus;
            if (hasFocus) {
                onFocus();
            } else {
                onBlur();
            }
        }
    }
}
