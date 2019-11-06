package se.mickelus.tetra.blocks.workbench.gui;

import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiString;
import se.mickelus.mgui.gui.GuiStringOutline;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.tetra.gui.GuiTextures;

public class GuiCapability extends GuiElement {

    protected Capability capability;

    private GuiString levelIndicator;

    public GuiCapability(int x, int y, Capability capability) {
        super(x, y, 16, 16);
        this.capability = capability;

        addChild(new GuiTexture(0, 0, 16, 16, capability.ordinal() * 16, 52, GuiTextures.workbench));

        levelIndicator = new GuiStringOutline(10, 8, "");
        addChild(levelIndicator);
    }

    public void update(int level, int color) {
        levelIndicator.setVisible(level >= 0);
        levelIndicator.setString(level + "");
        levelIndicator.setColor(color);
    }
}
