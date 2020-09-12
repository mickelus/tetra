package se.mickelus.tetra.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyModifier;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiRect;
import se.mickelus.mgui.gui.GuiStringOutline;
import se.mickelus.mgui.gui.impl.GuiColors;

import javax.annotation.Nullable;

public class GuiKeybinding extends GuiElement {

    public GuiKeybinding(int x, int y, KeyBinding keyBinding) {
        this(x, y,
                keyBinding.getKey().func_237520_d_().getString(),
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
            addChild(new GuiStringOutline(guiKey.getX() - 6, 2, "+", se.mickelus.mgui.gui.impl.GuiColors.muted));
        }
    }

    private class GuiKey extends GuiElement {

        public GuiKey(int x, int y, String key) {
            super(x, y, 0, 11);

            // todo 1.16: does this break width for single character elements
            if (key.length() == 1) {
                width = Minecraft.getInstance().fontRenderer.getStringWidth(key) + 5;
            } else {
                width = Minecraft.getInstance().fontRenderer.getStringWidth(key);
            }

            this.x = x - width;

            addChild(new GuiRect(0, 1, 1, height  - 2, se.mickelus.mgui.gui.impl.GuiColors.muted));
            addChild(new GuiRect(width - 1, 1, 1, height - 2, se.mickelus.mgui.gui.impl.GuiColors.muted));

            addChild(new GuiRect(1, 0, width - 2, 1, se.mickelus.mgui.gui.impl.GuiColors.muted));
            addChild(new GuiRect(1, height - 1, width - 2, 1, GuiColors.muted));

            addChild(new GuiStringOutline(3, 1, key.toLowerCase()));
        }
    }
}
