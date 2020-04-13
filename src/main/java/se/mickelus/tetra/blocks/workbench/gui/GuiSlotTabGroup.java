package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.resources.I18n;
import se.mickelus.mgui.gui.GuiElement;

import java.util.function.Consumer;

public class GuiSlotTabGroup extends GuiElement {
    private static final char[] keybindings = new char[] { 'a', 's', 'd'};
    private static final String[] labels = new String[] {
            I18n.format("tetra.workbench.slot_detail.details_tab"),
            I18n.format("tetra.workbench.slot_detail.craft_tab"),
            I18n.format("tetra.workbench.slot_detail.tweak_tab")
    };

    private GuiSlotTabButton[] buttons;
    private Consumer<Integer> clickHandler;

    public GuiSlotTabGroup(int x, int y, Consumer<Integer> clickHandler) {
        super(x, y, 3, 3 * 16 + 1);

        this.buttons = new GuiSlotTabButton[labels.length];

        for(int i = 0; i < labels.length; i++) {
            int index = i;
            this.buttons[i] = new GuiSlotTabButton(1, 1 + 16 * i, i, labels[i], String.valueOf(keybindings[i]), i == 1, () -> {
                clickHandler.accept(index);
                this.setActive(index);
            });
            this.addChild(this.buttons[i]);
        }

        this.clickHandler = clickHandler;
    }

    public void setActive(int index) {
        for(int i = 0; i < this.buttons.length; ++i) {
            this.buttons[i].setActive(i == index);
        }

    }

    public void setHasContent(int index, boolean hasContent) {
        this.buttons[index].setHasContent(hasContent);
    }

    public void keyTyped(char typedChar) {
        for(int i = 0; i < this.buttons.length; ++i) {
            if (i < keybindings.length && keybindings[i] == typedChar) {
                this.setActive(i);
                this.clickHandler.accept(i);
            }
        }

    }
}
