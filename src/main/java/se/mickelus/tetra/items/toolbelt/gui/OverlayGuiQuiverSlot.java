package se.mickelus.tetra.items.toolbelt.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.gui.animation.Applier;
import se.mickelus.tetra.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.impl.GuiColors;

public class OverlayGuiQuiverSlot extends GuiElement {

    private static final ResourceLocation texture = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/toolbelt-inventory.png");

    private ItemStack itemStack;

    private Minecraft mc;

    private KeyframeAnimation showAnimation;

    private FontRenderer fontRenderer;

    GuiTexture backdrop;
    GuiString count;
    GuiString hoverLabel;

    public OverlayGuiQuiverSlot(int x, int y, ItemStack itemStack, int slot) {
        super(x, y, 23, 23);

        setAttachmentPoint(GuiAttachment.bottomRight);
        setAttachmentAnchor(GuiAttachment.bottomRight);

        this.itemStack = itemStack;

        mc = Minecraft.getMinecraft();

        if (itemStack != null) {
            fontRenderer = itemStack.getItem().getFontRenderer(itemStack);
        }

        if (fontRenderer == null) {
            fontRenderer = mc.fontRenderer;
        }

        backdrop  = new GuiTexture(0, 0, 23, 23, 32, 28, texture);
        addChild(backdrop);

        if (itemStack != null) {
            count = new GuiStringOutline(-3, 1, itemStack.getCount() + "");
            count.setAttachmentPoint(GuiAttachment.middleLeft);
            count.setAttachmentAnchor(GuiAttachment.middleRight);
            addChild(count);
            count.setVisible(false);

            hoverLabel = new GuiString(-5, 0, itemStack.getDisplayName());
            hoverLabel.setAttachmentPoint(GuiAttachment.middleRight);
            hoverLabel.setAttachmentAnchor(GuiAttachment.middleLeft);
            addChild(hoverLabel);
            hoverLabel.setVisible(false);
        }


        isVisible = false;
        showAnimation = new KeyframeAnimation(80, this)
            .applyTo(
                    new Applier.TranslateX(x - 2, x),
                    new Applier.TranslateY(y + 2, y),
                    new Applier.Opacity(0, 1))
            .withDelay(slot * 80)
        .onStop((finished) -> count.setVisible(true));
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
        count.setVisible(false);
        hoverLabel.setVisible(false);
        return true;
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        super.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);

        if (this.opacity == 1) {
            drawItemStack(itemStack, x + refX + 3, y + refY + 3);
        }
    }

    private void drawItemStack(ItemStack itemStack, int x, int y) {
        GlStateManager.pushMatrix();
        GlStateManager.enableDepth();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderHelper.enableGUIStandardItemLighting();

        mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, x, y);
        mc.getRenderItem().renderItemOverlayIntoGUI(fontRenderer, itemStack, x, y, "");
        GlStateManager.disableDepth();

        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
    }


    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    protected void onFocus() {
        backdrop.setColor(GuiColors.hover);
        hoverLabel.setVisible(true);
    }

    @Override
    protected void onBlur() {
        backdrop.setColor(GuiColors.normal);
        hoverLabel.setVisible(false);
    }

    @Override
    protected void calculateFocusState(int refX, int refY, int mouseX, int mouseY) {
        mouseX -= refX + x;
        mouseY -= refY + y;
        boolean gainFocus = mouseX + mouseY >= 12 // NW
                && mouseX + mouseY <= 34 // SE
                && mouseX - mouseY < 10 // NE
                && mouseY - mouseX < 11; // SW

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
