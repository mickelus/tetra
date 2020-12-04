package se.mickelus.tetra.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyModifier;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiRect;
import se.mickelus.mgui.gui.GuiStringOutline;

import javax.annotation.Nullable;

public class GuiKeybinding extends GuiElement {

    public GuiKeybinding(int x, int y, KeyBinding keyBinding) {
        this(x, y,
                keyBinding.getKey().func_237520_d_().getString(),
                keyBinding.getKeyModifier() != KeyModifier.NONE ? keyBinding.getKeyModifier().toString() : null,
                I18n.format(keyBinding.getKeyDescription()));
    }

    public GuiKeybinding(int x, int y, KeyBinding keyBinding, GuiAttachment attachment) {
        this(x, y,
                keyBinding.getKey().func_237520_d_().getString(),
                keyBinding.getKeyModifier() != KeyModifier.NONE ? keyBinding.getKeyModifier().toString() : null,
                I18n.format(keyBinding.getKeyDescription()),
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

        GuiKey guiKey = new GuiKey(0, 0, key, GuiAttachment.topRight);
        addChild(guiKey);

        if (description != null) {
            addChild(new GuiStringOutline(3, 2, description));
        }

        if (modifier != null) {
            addChild(new GuiKey(guiKey.getX() - 7, 0, modifier, GuiAttachment.topRight));
            addChild(new GuiStringOutline(guiKey.getX() - 6, 2, "+", GuiColors.muted));
        }
    }

    public GuiKeybinding(int x, int y, String key, @Nullable String modifier, @Nullable String description, GuiAttachment attachment) {
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
        width += guiKey.getWidth() + 4;

        if (description != null) {
            GuiStringOutline descriptionElement = new GuiStringOutline(width, 2, description);
            addChild(descriptionElement);
            width += descriptionElement.getWidth();
        }
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
                width = Minecraft.getInstance().fontRenderer.getStringWidth(key) + 5;
            } else {
                width = Minecraft.getInstance().fontRenderer.getStringWidth(key);
            }

            addChild(new GuiRect(0, 1, 1, height - 2, GuiColors.muted));
            addChild(new GuiRect(width - 1, 1, 1, height - 2, GuiColors.muted));

            addChild(new GuiRect(1, 0, width - 2, 1, GuiColors.muted));
            addChild(new GuiRect(1, height - 1, width - 2, 1, GuiColors.muted));

            addChild(new GuiStringOutline(3, 1, key.toLowerCase()));
        }
    }
}
