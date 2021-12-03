package se.mickelus.tetra.items.modular.impl.toolbelt.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.gui.*;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class OverlayGuiQuiverSlot extends GuiElement {
    private ItemStack itemStack;

    private Minecraft mc;

    private KeyframeAnimation showAnimation;

    private Font fontRenderer;

    private GuiTexture backdrop;
    private GuiString count;
    private GuiString hoverLabel;

    public OverlayGuiQuiverSlot(int x, int y, ItemStack itemStack, int slot) {
        super(x, y, 23, 23);

        setAttachmentPoint(GuiAttachment.bottomRight);
        setAttachmentAnchor(GuiAttachment.bottomRight);

        this.itemStack = itemStack;

        mc = Minecraft.getInstance();

        if (itemStack != null) {
            fontRenderer = null; // itemStack.getItem().getFontRenderer(itemStack);
        }

        if (fontRenderer == null) {
            fontRenderer = mc.font;
        }

        backdrop  = new GuiTexture(0, 0, 23, 23, 32, 28, GuiTextures.toolbelt);
        addChild(backdrop);

        if (itemStack != null) {
            count = new GuiStringOutline(-3, 1, itemStack.getCount() + "");
            count.setAttachmentPoint(GuiAttachment.middleLeft);
            count.setAttachmentAnchor(GuiAttachment.middleRight);
            addChild(count);
            count.setVisible(false);

            hoverLabel = new GuiString(-5, 0, itemStack.getHoverName().getString());
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
    public void draw(PoseStack matrixStack, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        super.draw(matrixStack, refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);

        if (this.opacity == 1) {
            drawItemStack(itemStack, x + refX + 3, y + refY + 3);
        }
    }

    private void drawItemStack(ItemStack itemStack, int x, int y) {
        PoseStack renderSystemStack = RenderSystem.getModelViewStack();
        renderSystemStack.pushPose();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        // Lighting.turnBackOn();

        mc.getItemRenderer().renderAndDecorateItem(itemStack, x, y);
        mc.getItemRenderer().renderGuiItemDecorations(fontRenderer, itemStack, x, y, "");
      //  Lighting.turnOff();
        RenderSystem.disableDepthTest();
        renderSystemStack.popPose();
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
