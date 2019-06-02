package se.mickelus.tetra.items.journal.gui.craft;

import se.mickelus.tetra.gui.GuiButton;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.GuiString;
import se.mickelus.tetra.gui.animation.Applier;
import se.mickelus.tetra.gui.animation.KeyframeAnimation;

import java.util.function.Consumer;

public class GuiJournalBreadcrumbs extends GuiElement {

    private Consumer<Integer> onClick;

    private KeyframeAnimation showAnimation;
    private KeyframeAnimation hideAnimation;

    public GuiJournalBreadcrumbs(int x, int y, int width, Consumer<Integer> onClick) {
        super(x, y, width, 16);

        this.onClick = onClick;

        showAnimation = new KeyframeAnimation(80, this)
                .applyTo(new Applier.Opacity(1), new Applier.TranslateX(x));

        hideAnimation = new KeyframeAnimation(80, this)
                .applyTo(new Applier.Opacity(0), new Applier.TranslateX(x - 5))
                .onStop(complete -> this.isVisible = false);
    }

    public void setItems(String[] items) {
        clearChildren();

        int xOffset = 0;

        for (int i = 0; i < items.length; i++) {
            final int index = i;
            GuiButton button = new GuiButton(xOffset, 4, items[i], () -> onClick.accept(index));
            addChild(button);

            xOffset += button.getWidth() + 12;
            if (i < items.length - 1) {
                addChild(new GuiString(xOffset - 8, 5, ">", GuiColors.muted));
            }
        }
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
}
