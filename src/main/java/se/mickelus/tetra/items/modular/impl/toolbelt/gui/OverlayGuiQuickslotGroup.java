package se.mickelus.tetra.items.modular.impl.toolbelt.gui;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiRect;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.QuickslotInventory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Objects;
@ParametersAreNonnullByDefault
public class OverlayGuiQuickslotGroup extends GuiElement {

    private KeyframeAnimation showAnimation;
    private KeyframeAnimation hideAnimation;

    private OverlayGuiQuickslot[] slots = new OverlayGuiQuickslot[0];

    public OverlayGuiQuickslotGroup(int x, int y) {
        super(x, y, 200, 0);
        isVisible = false;
        opacity = 0;
        showAnimation = new KeyframeAnimation(200, this)
                .applyTo(new Applier.TranslateX(this.getX() + 10), new Applier.Opacity(1))
                .onStop(isFinished -> {
                    if (isFinished) {
                        Arrays.stream(slots)
                                .filter(Objects::nonNull)
                                .forEach(item -> item.setVisible(true));
                    }
                });
        hideAnimation = new KeyframeAnimation(100, this)
                .applyTo(new Applier.TranslateX(this.getX()), new Applier.Opacity(0))
                .onStop(isFinished -> {
                    if (isFinished) {
                        isVisible = false;
                    }
                });
    }

    public void setInventory(QuickslotInventory inventory) {
        clearChildren();
        int numSlots = inventory.getContainerSize();
        slots = new OverlayGuiQuickslot[numSlots];

        addChild(new GuiTexture(0, numSlots * -OverlayGuiQuickslot.height / 2 - 9, 22, 7, 0, 28, GuiTextures.toolbelt));
        addChild(new GuiTexture(0, numSlots * OverlayGuiQuickslot.height / 2 + 2, 22, 7, 0, 35, GuiTextures.toolbelt));
        addChild(new GuiRect(0, numSlots * -OverlayGuiQuickslot.height / 2 - 2, 22, numSlots * OverlayGuiQuickslot.height + 4, 0xcc000000));

        for (int i = 0; i < numSlots; i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (!itemStack.isEmpty()) {
                slots[i] = new OverlayGuiQuickslot(-35, numSlots * -OverlayGuiQuickslot.height / 2 + i * OverlayGuiQuickslot.height, itemStack, i);
                addChild(slots[i]);
            }
        }
    }

    @Override
    protected void onShow() {
        hideAnimation.stop();

        if (slots.length > 0) {
            showAnimation.start();
        }
    }

    @Override
    protected boolean onHide() {
        showAnimation.stop();

        hideAnimation.start();
        Arrays.stream(slots)
                .filter(Objects::nonNull)
                .forEach(item -> item.setVisible(false));
        return false;
    }

    public int getFocus() {
        return Arrays.stream(slots)
                .filter(Objects::nonNull)
                .filter(GuiElement::hasFocus)
                .map(OverlayGuiQuickslot::getSlot)
                .findFirst()
                .orElse(-1);
    }

    public InteractionHand getHand() {
        return Arrays.stream(slots)
                .filter(Objects::nonNull)
                .filter(GuiElement::hasFocus)
                .map(OverlayGuiQuickslot::getHand)
                .findFirst()
                .orElse(null);
    }
}
