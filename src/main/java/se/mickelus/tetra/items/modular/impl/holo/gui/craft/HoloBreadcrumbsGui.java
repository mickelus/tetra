package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiButton;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiString;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiKeybinding;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class HoloBreadcrumbsGui extends GuiElement {

    private final Consumer<Integer> onClick;

    private final KeyframeAnimation openAnimation;
    private final KeyframeAnimation showAnimation;
    private final KeyframeAnimation hideAnimation;

    private final KeyframeAnimation focusAnimation;
    private final KeyframeAnimation blurAnimation;

    private final List<GuiElement> separators;
    private final List<GuiButton> buttons;

    public HoloBreadcrumbsGui(int x, int y, int width, Consumer<Integer> onClick) {
        super(x, y, width, 16);

        this.onClick = onClick;

        openAnimation = new KeyframeAnimation(80, this)
                .applyTo(new Applier.Opacity(0, 1), new Applier.TranslateX(x - 5, x))
                .withDelay(80);

        showAnimation = new KeyframeAnimation(80, this)
                .applyTo(new Applier.Opacity(1), new Applier.TranslateX(x));

        hideAnimation = new KeyframeAnimation(80, this)
                .applyTo(new Applier.Opacity(0), new Applier.TranslateX(x - 5))
                .onStop(complete -> this.isVisible = false);

        separators = new ArrayList<>();
        buttons = new ArrayList<>();

        GuiKeybinding keybinding = new GuiKeybinding(0, 3, "" + HoloCraftRootGui.backBinding);
        keybinding.setAttachmentPoint(GuiAttachment.topRight);
        keybinding.setOpacity(0);
        addChild(keybinding);
        focusAnimation = new KeyframeAnimation(80, keybinding)
                .applyTo(new Applier.Opacity(1), new Applier.TranslateX(keybinding.getX() - 5));

        blurAnimation = new KeyframeAnimation(80, keybinding)
                .applyTo(new Applier.Opacity(0), new Applier.TranslateX(keybinding.getX()));
    }

    public void setItems(String[] items) {

        // update overlapping buttons
        int xOffset = 0;
        int overlapCount = Math.min(buttons.size(), items.length);
        for (int i = 0; i < overlapCount; i++) {
            String label = !"".equals(items[i]) ? items[i] : I18n.get("tetra.holo.craft.slot");
            GuiButton button = buttons.get(i);
            button.setText(label);
            button.setX(xOffset);

            if (i != 0) {
                separators.get(i - 1).setX(xOffset - 8);
            }

            xOffset = button.getX() + button.getWidth() + 12;
        }

        // remove no longer existing items
        for (int i = buttons.size(); i > items.length; i--) {
            removeButton(i - 1);
        }

        // add new items
        for (int i = buttons.size(); i < items.length; i++) {
            String label = !"".equals(items[i]) ? items[i] : I18n.get("tetra.holo.craft.slot");

            addButton(i, label);
        }

        // update width based on number of buttons
        if (!buttons.isEmpty()) {
            GuiButton button = buttons.get(buttons.size() - 1);
            setWidth(button.getX() + button.getWidth());
        }
    }

    private void addButton(int index, String label) {
        int xOffset = 0;

        if (!buttons.isEmpty()) {
            GuiElement last = buttons.get(buttons.size() - 1);
            xOffset = last.getX() + last.getWidth() + 12;
        }

        GuiButton button = new GuiButton(xOffset, 4, ChatFormatting.stripFormatting(label), () -> onClick.accept(index));
        new KeyframeAnimation(80, button)
                .applyTo(new Applier.Opacity(0, 1), new Applier.TranslateX(xOffset - 2, xOffset))
                .withDelay(40)
                .start();

        buttons.add(button);
        addChild(button);

        if (index != 0) {
            GuiElement separator = new GuiString(xOffset - 8, 5, ">", GuiColors.muted);
            new KeyframeAnimation(80, separator)
                    .applyTo(new Applier.Opacity(0, 1), new Applier.TranslateX(xOffset - 10, xOffset - 8))
                    .start();

            separators.add(separator);
            addChild(separator);
        }
    }

    private void removeButton(int index) {
        GuiElement button = buttons.remove(index);
        new KeyframeAnimation(80, button)
                .applyTo(new Applier.Opacity(0), new Applier.TranslateX(button.getX() - 2))
                .onStop(finished -> button.remove())
                .start();


        if (index > 0) {
            GuiElement separator = separators.remove(index - 1);
            new KeyframeAnimation(80, separator)
                    .applyTo(new Applier.Opacity(0), new Applier.TranslateX(separator.getX() - 2))
                    .withDelay(40)
                    .onStop(finished -> separator.remove())
                    .start();
        }
    }

    public void animateOpen(boolean fast) {
        openAnimation
                .withDelay(fast ? 80 : 600)
                .start();
    }

    @Override
    protected void onShow() {
        super.onShow();
        hideAnimation.stop();
        showAnimation.start();
    }

    @Override
    protected boolean onHide() {
        super.onHide();
        showAnimation.stop();
        hideAnimation.start();

        return false;
    }

    @Override
    protected void onFocus() {
        if (buttons.size() > 0) {
            blurAnimation.stop();
            focusAnimation.start();
        }
    }

    @Override
    protected void onBlur() {
        focusAnimation.stop();
        blurAnimation.start();
    }
}
