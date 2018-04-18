package se.mickelus.tetra.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyModifier;

public class GuiKeybinding extends GuiElement {

    public GuiKeybinding(int x, int y, KeyBinding keyBinding) {
        super(x, y, 0, 0);


        GuiKey guiKey = new GuiKey(0, 0, GameSettings.getKeyDisplayString(keyBinding.getKeyCode()));
        addChild(guiKey);

        addChild(new GuiStringOutline(3, 2, I18n.format(keyBinding.getKeyDescription())));

        if (keyBinding.getKeyModifier() != KeyModifier.NONE) {
            addChild(new GuiKey(guiKey.x - 7, 0, keyBinding.getKeyModifier().toString()));
            addChild(new GuiStringOutline(guiKey.x - 6, 2, "+", GuiColors.muted));
        }
    }

    private class GuiKey extends GuiElement {

        public GuiKey(int x, int y, String key) {
            super(x, y, 0, 11);

            if (key.length() == 1) {
                width = Minecraft.getMinecraft().fontRenderer.getCharWidth(key.charAt(0)) + 5;
            } else {
                width = Minecraft.getMinecraft().fontRenderer.getStringWidth(key);
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
