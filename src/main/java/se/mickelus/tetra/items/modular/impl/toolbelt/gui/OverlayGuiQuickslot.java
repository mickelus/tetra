package se.mickelus.tetra.items.modular.impl.toolbelt.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import se.mickelus.mgui.gui.GuiItem;
import se.mickelus.mgui.gui.GuiString;
import se.mickelus.mgui.gui.GuiStringOutline;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;

public class OverlayGuiQuickslot extends GuiElement {
    public static final int height = 20;

    private ItemStack itemStack;

    private int slot;

    private KeyframeAnimation showAnimation;

    private KeyframeAnimation showLabel;
    private KeyframeAnimation hideLabel;

    private GuiItem guiItem;
    private GuiString label;
    private OverlayGuiQuickslotSide hitLeft;
    private OverlayGuiQuickslotSide hitRight;

    public OverlayGuiQuickslot(int x, int y, ItemStack itemStack, int slot) {
        super(x, y, 200, height);

        this.itemStack = itemStack;
        this.slot = slot;

        guiItem = new GuiItem(38, 1);
        guiItem.setOpacity(0);
        guiItem.setOpacityThreshold(0.2f);
        guiItem.setItem(itemStack);
        addChild(guiItem);

        label = new GuiStringOutline(61, 6, itemStack.getDisplayName().getString());
        label.setColor(GuiColors.hover);
        label.setOpacity(0);
        addChild(label);

        hitLeft = new OverlayGuiQuickslotSide(0, 0, 46, height, false);
        addChild(hitLeft);
        hitRight = new OverlayGuiQuickslotSide(46, 0, 151, height, true);
        addChild(hitRight);

        isVisible = false;
        showAnimation = new KeyframeAnimation(80, guiItem)
            .applyTo(new Applier.Opacity(1), new Applier.TranslateY(0, 1))
            .withDelay(slot * 80);


        showLabel = new KeyframeAnimation(100, label)
                .applyTo(new Applier.TranslateX(label.getX() + 1), new Applier.Opacity(1));
        hideLabel = new KeyframeAnimation(200, label)
                .applyTo(new Applier.TranslateX(label.getX()), new Applier.Opacity(0));
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
    }

    @Override
    protected void onFocus() {
        super.onFocus();
        hideLabel.stop();
        showLabel.start();
        hitLeft.animateIn();
        hitRight.animateIn();
    }

    @Override
    protected void onBlur() {
        super.onBlur();
        showLabel.stop();
        hideLabel.start();
        hitLeft.animateOut();
        hitRight.animateOut();
    }

    public int getSlot() {
        return slot;
    }

    public Hand getHand() {
        return (Minecraft.getInstance().player.getPrimaryHand() == HandSide.RIGHT) == hitRight.hasFocus()
                ? Hand.MAIN_HAND
                : Hand.OFF_HAND;
    }
}
