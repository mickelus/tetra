package se.mickelus.tetra.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.KeyModifier;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiRect;
import se.mickelus.mgui.gui.GuiStringOutline;

import javax.annotation.Nullable;
import java.util.Objects;

public class GuiKeybinding extends GuiElement {

    public GuiKeybinding(int x, int y, KeyBinding keyBinding) {
        this(x, y,
                getLocalizedKey(keyBinding.getKey()),
                keyBinding.getKeyModifier() != KeyModifier.NONE ? keyBinding.getKeyModifier().toString() : null,
                I18n.format(keyBinding.getKeyDescription()));
    }

    public GuiKeybinding(int x, int y, String key, @Nullable String modifier) {
        this(x, y, key, modifier, null);
    }

    public GuiKeybinding(int x, int y, String key) {
        this(x, y, key, null, null);
    }

    public GuiKeybinding(int x, int y, String key, @Nullable String modifier, @Nullable String description) {
        super(x, y, 0, 0);

        GuiKey guiKey = new GuiKey(0, 0, key);
        addChild(guiKey);

        if (description != null) {
            addChild(new GuiStringOutline(3, 2, description));
        }

        if (modifier != null) {
            addChild(new GuiKey(guiKey.getX() - 7, 0, modifier));
            addChild(new GuiStringOutline(guiKey.getX() - 6, 2, "+", GuiColors.muted));
        }
    }

    // Based on {@KeyBinding.getLocalizedName} but skips the modifier, since we plop that in a separate box
    private static String getLocalizedKey(InputMappings.Input keyCode) {
        String s = keyCode.getTranslationKey();
        int i = keyCode.getKeyCode();
        String s1 = null;

        switch(keyCode.getType()) {
            case KEYSYM:
                s1 = InputMappings.getKeynameFromKeycode(i);
                break;
            case SCANCODE:
                s1 = InputMappings.getKeyNameFromScanCode(i);
                break;
            case MOUSE:
                String s2 = I18n.format(s);
                s1 = Objects.equals(s2, s) ? I18n.format(InputMappings.Type.MOUSE.name(), i + 1) : s2;
        }

        return s1 == null ? I18n.format(s) : s1;
    }

    private class GuiKey extends GuiElement {

        public GuiKey(int x, int y, String key) {
            super(x, y, 0, 11);

            width = Minecraft.getInstance().fontRenderer.getStringWidth(key);

            // need to add some extra padding for single character strings
            if (key.length() == 1) {
                width += 5;
            }

            this.x = x - width;

            addChild(new GuiRect(0, 1, 1, height  - 2, GuiColors.muted));
            addChild(new GuiRect(width - 1, 1, 1, height - 2, GuiColors.muted));

            addChild(new GuiRect(1, 0, width - 2, 1, GuiColors.muted));
            addChild(new GuiRect(1, height - 1, width - 2, 1, GuiColors.muted));

            addChild(new GuiStringOutline(3, 1, key.toLowerCase()));
        }
    }
}
