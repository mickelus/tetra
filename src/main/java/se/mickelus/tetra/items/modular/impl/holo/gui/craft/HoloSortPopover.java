package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import org.lwjgl.glfw.GLFW;
import se.mickelus.mutil.gui.*;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.mutil.gui.impl.GuiVerticalLayoutGroup;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiKeybinding;
import se.mickelus.tetra.gui.stats.sorting.IStatSorter;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
@ParametersAreNonnullByDefault
public class HoloSortPopover extends GuiElement {
    private Consumer<IStatSorter> onSelect;

    private GuiVerticalLayoutGroup items;
    private GuiElement backdrop;

    private final KeyframeAnimation showAnimation;
    private final KeyframeAnimation hideAnimation;

    public HoloSortPopover(int x, int y, Consumer<IStatSorter> onSelect) {
        super(x, y - 3, 40, 9);

        backdrop = new GuiRect(0, 0, width, 0, 0).setOpacity(0.9f);
        addChild(backdrop);
        addChild(new GuiRect(1, 1, 6, 1, GuiColors.normal));
        addChild(new GuiRect(-1, 1, 6, 1, GuiColors.normal).setAttachment(GuiAttachment.topRight));
        addChild(new GuiRect(-1, -1, 6, 1, GuiColors.normal).setAttachment(GuiAttachment.bottomRight));
        addChild(new GuiRect(1, -1, 6, 1, GuiColors.normal).setAttachment(GuiAttachment.bottomLeft));

        items = new GuiVerticalLayoutGroup(6, 6, 40, 3);
        addChild(items);

        this.onSelect = onSelect;

        isVisible = false;

        showAnimation = new KeyframeAnimation(150, this)
                .applyTo(new Applier.TranslateY(y), new Applier.Opacity(1));
        hideAnimation = new KeyframeAnimation(100, this)
                .applyTo(new Applier.TranslateY(y - 3), new Applier.Opacity(0))
                .onStop(complete -> { if (complete) isVisible = false; });
    }

    public void update(IStatSorter[] sorters) {
        items.clearChildren();

        int maxWidth = 0;

        for (int i = 0; i < sorters.length; i++) {
            Item item = new Item(0, 0, i, sorters[i], this::onSelect);
            items.addChild(item);

            if (item.getWidth() > maxWidth) {
                maxWidth = item.getWidth();
            }
        }
        items.forceLayout();

        setHeight(items.getHeight() + 12);
        setWidth(maxWidth + 12);
        backdrop.setHeight(getHeight());
        backdrop.setWidth(getWidth());
    }

    public void onSelect(IStatSorter sorter) {
        onSelect.accept(sorter);
        setVisible(false);
    }

    @Override
    protected void onShow() {
        if (!showAnimation.isActive()) {
            showAnimation.start();
        }
        hideAnimation.stop();
    }

    @Override
    protected boolean onHide() {
        if (!hideAnimation.isActive()) {
            hideAnimation.start();
        }
        showAnimation.stop();
        return false;
    }

    @Override
    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            setVisible(false);
            return true;
        }

        return super.onKeyPress(keyCode, scanCode, modifiers);
    }

    static class Item extends GuiClickable {
        GuiString label;

        GuiElement keybinding;
        KeyframeAnimation showKeybind;
        KeyframeAnimation hideKeybind;

        int index;

        public Item(int x, int y, int index, IStatSorter statSorter, Consumer<IStatSorter> onClickHandler) {
            super(x, y, 40, 10, () -> onClickHandler.accept(statSorter));

            label = new GuiString(0, 0, statSorter.getName());
            addChild(label);

            setWidth(label.getWidth());

            this.index = index;

            if (index < 10) {
                GuiKeybinding inner = new GuiKeybinding(1, 1, (index + 1) + "");
                keybinding = new GuiElement(-10, -2, inner.getWidth() + 2, inner.getHeight() + 2);
                keybinding.addChild(new GuiRect(0, 0, keybinding.getWidth(), keybinding.getHeight(), 0).setOpacity(0.9f));
                keybinding.addChild(inner);
                keybinding.setOpacity(0);
                keybinding.setAttachmentPoint(GuiAttachment.topRight);
                addChild(keybinding);

                showKeybind = new KeyframeAnimation(150, keybinding)
                        .applyTo(new Applier.TranslateX(-7), new Applier.Opacity(1))
                        .withDelay(index * 60);
                hideKeybind = new KeyframeAnimation(100, keybinding)
                        .applyTo(new Applier.TranslateX(-10), new Applier.Opacity(0));
            }
        }

        @Override
        protected void onFocus() {
            label.setColor(GuiColors.hover);
            super.onFocus();
        }

        @Override
        protected void onBlur() {
            label.setColor(GuiColors.normal);
            super.onBlur();
        }

        @Override
        public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
            if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
                if (keybinding != null) {
                    if (hideKeybind.isActive()) {
                        hideKeybind.stop();
                    }

                    if (!showKeybind.isActive()) {
                        showKeybind.start();
                    }
                }
            }

            return false;
        }

        @Override
        public boolean onKeyRelease(int keyCode, int scanCode, int modifiers) {
            if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT) {
                if (keybinding != null) {
                    if (showKeybind.isActive()) {
                        showKeybind.stop();
                    }

                    if (!hideKeybind.isActive()) {
                        hideKeybind.start();
                    }
                }
            }

            return false;
        }

        @Override
        public boolean onCharType(char character, int modifiers) {
            if (Character.getNumericValue(character) == index + 1) {
                onClickHandler.run();
                return true;
            }

            return super.onCharType(character, modifiers);
        }
    }
}
