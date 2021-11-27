package se.mickelus.tetra.gui;

import net.minecraft.util.ResourceLocation;
import se.mickelus.mgui.gui.GuiElement;

import java.util.function.Consumer;

public class VerticalTabGroupGui extends GuiElement {

    private static final char[] keybindings = new char[] {'a', 's', 'd', 'f', 'g'};

    private VerticalTabButtonGui[] buttons;

    private Consumer<Integer> clickHandler;

    public VerticalTabGroupGui(int x, int y, Consumer<Integer> clickHandler, String ... labels) {
        super(x, y, 3, labels.length * 16 + 1);

        buttons = new VerticalTabButtonGui[labels.length];

        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            buttons[i] = new VerticalTabButtonGui(1, 1 + 16 * i, labels[i],
                    i < keybindings.length ? keybindings[i] + "" : null,
                    () -> {
                        clickHandler.accept(index);
                        setActive(index);
                    },
                    i == 0);
            addChild(buttons[i]);
        }

        this.clickHandler = clickHandler;
    }

    public VerticalTabGroupGui(int x, int y, Consumer<Integer> clickHandler, ResourceLocation texture, int textureX, int textureY, String ... labels) {
        super(x, y, 3, labels.length * 16 + 1);

        buttons = new VerticalTabButtonGui[labels.length];

        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            buttons[i] = new VerticalTabIconButtonGui(1, 1 + 16 * i, texture, textureX + 16 * i, textureY, labels[i],
                    i < keybindings.length ? keybindings[i] + "" : null,
                    () -> {
                        clickHandler.accept(index);
                        setActive(index);
                    },
                    i == 0);
            addChild(buttons[i]);
        }

        this.clickHandler = clickHandler;
    }

    public void setActive(int index) {
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setActive(i == index);
        }
    }

    public void setHasContent(int index, boolean hasContent) {
        buttons[index].setHasContent(hasContent);
    }

    public void keyTyped(char typedChar) {
        for (int i = 0; i < buttons.length; i++) {
            if (i < keybindings.length && keybindings[i] == typedChar) {
                setActive(i);
                clickHandler.accept(i);
            }
        }
    }
}
