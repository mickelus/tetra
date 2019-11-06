package se.mickelus.tetra.items.journal.gui;

import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiButton;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiRect;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.items.journal.JournalPage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class GuiJournalHeader extends GuiElement {

    private final GuiButton[] buttons;

    private final List<KeyframeAnimation> showAnimations;

    public GuiJournalHeader(int x, int y, int width, Consumer<JournalPage> onPageChange) {
        super(x, y, width, 12);

        showAnimations = new ArrayList<>();

        buttons = Arrays.stream(JournalPage.values())
                .map(page -> new GuiButton(0, 4, page.label, () -> onPageChange.accept(page)))
                .toArray(GuiButton[]::new);

        // position buttons
        int spacing = width / (buttons.length + 1);
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setAttachmentPoint(GuiAttachment.topCenter);
            buttons[i].setX((i + 1) * spacing);
            addChild(buttons[i]);
        }

        changePage(JournalPage.craft);
        for (int i = 0; i < buttons.length; i++) {
            KeyframeAnimation animation = getButtonAnimation(buttons[i], i, buttons.length);
            showAnimations.add(animation);
        }

        // separators
        setupSeparators();
    }

    private KeyframeAnimation getButtonAnimation(GuiElement button, int i, int size) {
        int delay = 1 + Math.abs(i - size / 2);
        if (i == (size + 1) / 2 - 1 && size % 2 != 0) {
            return new KeyframeAnimation(200, button)
                    .withDelay(delay * 300)
                    .applyTo(new Applier.TranslateY(3, 0, true), new Applier.Opacity(0, 0, false, true));
        } else if (i < size / 2) {
            return new KeyframeAnimation(200, button)
                    .withDelay(delay * 300)
                    .applyTo(new Applier.TranslateX(5, 0, true), new Applier.Opacity(0, 0, false, true));
        } else {
            return new KeyframeAnimation(200, button)
                    .withDelay(delay * 300)
                    .applyTo(new Applier.TranslateX(-5, 0, true), new Applier.Opacity(0, 0, false, true));
        }

    }

    private void setupSeparators() {
        GuiElement separator = new GuiRect(0, 0, width, 1, GuiColors.separator);
        separator.setAttachment(GuiAttachment.topCenter);
        showAnimations.add(new KeyframeAnimation(800, separator)
                .applyTo(new Applier.Width(width / 2f, width), new Applier.Opacity(0, 0.3f)));
        addChild(separator);

        separator = new GuiRect(0, 0, width, 1, GuiColors.separator);
        separator.setAttachment(GuiAttachment.topCenter);
        showAnimations.add(new KeyframeAnimation(200, separator)
                .applyTo(new Applier.Width(0, width), new Applier.Opacity(0, 0.3f)));
        addChild(separator);

        separator = new GuiRect(0, 16, width, 1, GuiColors.separator);
        separator.setAttachment(GuiAttachment.topCenter);
        showAnimations.add(new KeyframeAnimation(800, separator)
                .applyTo(new Applier.Width(width / 1.25f, width), new Applier.Opacity(0, 0.3f)));
        addChild(separator);

        separator = new GuiRect(0, 16, width, 1, GuiColors.separator);
        separator.setAttachment(GuiAttachment.topCenter);
        showAnimations.add(new KeyframeAnimation(200, separator)
                .applyTo(new Applier.Width(width / 2f, width), new Applier.Opacity(0, 0.3f)));
        addChild(separator);
    }

    @Override
    protected void onShow() {
        showAnimations.forEach(KeyframeAnimation::start);
    }

    public void changePage(JournalPage page) {
        for (GuiButton button: buttons) {
            button.setOpacity(0.5f);
        }

        buttons[page.ordinal()].setOpacity(1);
    }
}
