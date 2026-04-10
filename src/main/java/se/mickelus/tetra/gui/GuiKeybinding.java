package se.mickelus.tetra.gui;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.neoforged.neoforge.client.settings.KeyModifier;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiRect;
import se.mickelus.mutil.gui.GuiString;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class GuiKeybinding extends GuiElement {

    public GuiKeybinding(int x, int y, KeyMapping keyBinding) {
        this(x, y,
                keyBinding.getKey().getDisplayName().getString(),
                keyBinding.getKeyModifier() != KeyModifier.NONE ? keyBinding.getKeyModifier().toString() : null,
                I18n.get(keyBinding.getName()));
    }

    public GuiKeybinding(int x, int y, KeyMapping keyBinding, GuiAttachment attachment) {
        this(x, y,
                keyBinding.getKey().getDisplayName().getString(),
                keyBinding.getKeyModifier() != KeyModifier.NONE ? keyBinding.getKeyModifier().toString() : null,
                I18n.get(keyBinding.getName()),
                attachment);
    }

    public GuiKeybinding(int x, int y, String key, @Nullable String modifier) {
        this(x, y, key, modifier, null);
    }

    public GuiKeybinding(int x, int y, String key) {
        this(x, y, key, null, null);
    }

    public GuiKeybinding(int x, int y, String key, @Nullable String modifier, @Nullable String description) {
        super(x, y, 0, 11);
        if (modifier != null) {
            GuiKey modifierKey = new GuiKey(0, 0, formatModifier(modifier));
            addChild(modifierKey);

            GuiString joiner = new GuiString(modifierKey.getWidth() + 2, 2, "+", GuiColors.muted);
            addChild(joiner);

            width = modifierKey.getWidth() + 2 + joiner.getWidth() + 2;
        }

        GuiKey guiKey = new GuiKey(width, 0, key.toUpperCase());
        addChild(guiKey);
        width += guiKey.getWidth();

        if (description != null) {
            width += 4;
            GuiString descriptionElement = new GuiString(width, 2, description);
            addChild(descriptionElement);
            width += descriptionElement.getWidth();
        }
    }

    public GuiKeybinding(int x, int y, String key, @Nullable String modifier, @Nullable String description, GuiAttachment attachment) {
        this(x, y, key, modifier, description);
        setAttachment(attachment);
    }

    private static String formatModifier(String modifier) {
        if ("CONTROL".equals(modifier)) {
            return "ctrl";
        }

        return modifier.toLowerCase();
    }

    private class GuiKey extends GuiElement {

        public GuiKey(int x, int y, String key) {
            this(x, y, key, GuiAttachment.topLeft);
        }

        public GuiKey(int x, int y, String key, GuiAttachment attachment) {
            super(x, y, 0, 11);

            setAttachment(attachment);

            width = Minecraft.getInstance().font.width(key) + 5;

            addChild(new GuiRect(-1, 0, 1, height, GuiColors.muted));
            addChild(new GuiRect(width, 0, 1, height, GuiColors.muted));

            addChild(new GuiRect(0, -1, width, 1, GuiColors.muted));
            addChild(new GuiRect(0, height, width, 1, GuiColors.muted));

            addChild(new GuiString(3, 2, key));
        }
    }
}
