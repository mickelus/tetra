package se.mickelus.tetra.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyModifier;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiRect;
import se.mickelus.mgui.gui.GuiStringOutline;

import javax.annotation.Nullable;

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
            GuiKey modifierKey = new GuiKey(0, 0, modifier);
            addChild(modifierKey);

            GuiStringOutline joiner = new GuiStringOutline(modifierKey.getWidth() + 2, 2, "+", GuiColors.muted);
            addChild(joiner);

            width = modifierKey.getWidth() + 2 + joiner.getWidth() + 2;
        }

        GuiKey guiKey = new GuiKey(width, 0, key);
        addChild(guiKey);
        width += guiKey.getWidth();

        if (description != null) {
            width += 4;
            GuiStringOutline descriptionElement = new GuiStringOutline(width, 2, description);
            addChild(descriptionElement);
            width += descriptionElement.getWidth();
        }
    }

    public GuiKeybinding(int x, int y, String key, @Nullable String modifier, @Nullable String description, GuiAttachment attachment) {
        this(x, y, key, modifier, description);
        setAttachment(attachment);
    }

    private class GuiKey extends GuiElement {

        public GuiKey(int x, int y, String key) {
            this(x, y, key, GuiAttachment.topLeft);
        }

        public GuiKey(int x, int y, String key, GuiAttachment attachment) {
            super(x, y, 0, 11);

            setAttachment(attachment);

            // todo 1.16: does this break width for single character elements
            if (key.length() == 1) {
                width = Minecraft.getInstance().font.width(key) + 5;
            } else {
                width = Minecraft.getInstance().font.width(key);
            }

            addChild(new GuiRect(0, 1, 1, height - 2, GuiColors.muted));
            addChild(new GuiRect(width - 1, 1, 1, height - 2, GuiColors.muted));

            addChild(new GuiRect(1, 0, width - 2, 1, GuiColors.muted));
            addChild(new GuiRect(1, height - 1, width - 2, 1, GuiColors.muted));

            addChild(new GuiStringOutline(3, 1, key.toLowerCase()));
        }
    }
}
