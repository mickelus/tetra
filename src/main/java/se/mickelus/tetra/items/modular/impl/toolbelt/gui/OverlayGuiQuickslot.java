package se.mickelus.tetra.items.modular.impl.toolbelt.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiItem;
import se.mickelus.mutil.gui.GuiString;
import se.mickelus.mutil.gui.GuiStringOutline;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.GuiColors;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class OverlayGuiQuickslot extends GuiElement {
    public static final int height = 20;

    private final ItemStack itemStack;

    private final int slot;

    private final KeyframeAnimation showAnimation;

    private final KeyframeAnimation showLabel;
    private final KeyframeAnimation hideLabel;

    private final GuiItem guiItem;
    private final GuiString label;
    private final OverlayGuiQuickslotSide hitLeft;
    private final OverlayGuiQuickslotSide hitRight;

    public OverlayGuiQuickslot(int x, int y, ItemStack itemStack, int slot) {
        super(x, y, 200, height);

        this.itemStack = itemStack;
        this.slot = slot;

        guiItem = new GuiItem(38, 1);
        guiItem.setOpacity(0);
        guiItem.setOpacityThreshold(0.2f);
        guiItem.setItem(itemStack);
        addChild(guiItem);

        label = new GuiStringOutline(61, 6, itemStack.getHoverName().getString());
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
    public void draw(PoseStack matrixStack, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
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

    public InteractionHand getHand() {
        return (Minecraft.getInstance().player.getMainArm() == HumanoidArm.RIGHT) == hitRight.hasFocus()
                ? InteractionHand.MAIN_HAND
                : InteractionHand.OFF_HAND;
    }
}
