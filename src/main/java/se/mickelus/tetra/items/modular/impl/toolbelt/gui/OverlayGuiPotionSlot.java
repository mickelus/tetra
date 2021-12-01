package se.mickelus.tetra.items.modular.impl.toolbelt.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;

public class OverlayGuiPotionSlot extends GuiElement {
    private ItemStack itemStack;

    private int slot;

    private Minecraft mc;

    private KeyframeAnimation showAnimation;

    private Font fontRenderer;

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
            fontRenderer = mc.font;
        }

        backdrop  = new GuiTexture(0, 0, 23, 23, 32, 28, GuiTextures.toolbelt);
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
    public void draw(PoseStack matrixStack, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        super.draw(matrixStack, refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);

        if (this.opacity == 1) {
            drawItemStack(itemStack, x + refX + 3, y + refY + 2);
        }
    }

    private void drawItemStack(ItemStack itemStack, int x, int y) {
        RenderSystem.pushMatrix();
        GlStateManager._enableDepthTest();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        Lighting.turnBackOn();

        mc.getItemRenderer().renderAndDecorateItem(itemStack, x, y);
        mc.getItemRenderer().renderGuiItemDecorations(fontRenderer, itemStack, x, y, "");
        GlStateManager._disableDepthTest();

        RenderSystem.popMatrix();
        Lighting.turnOff();
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
