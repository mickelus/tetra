package se.mickelus.tetra.blocks.workbench.gui;

import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.GuiString;
import se.mickelus.tetra.gui.GuiStringOutline;
import se.mickelus.tetra.gui.GuiTexture;

public class GuiCapability extends GuiElement {

    private static final String texture = "textures/gui/workbench.png";

    protected Capability capability;

    private GuiString levelIndicator;

    public GuiCapability(int x, int y, Capability capability) {
        super(x, y, 16, 16);
        this.capability = capability;

        addChild(new GuiTexture(0, 0, 16, 16, capability.ordinal() * 16, 52, texture));

        levelIndicator = new GuiStringOutline(10, 8, "");
        addChild(levelIndicator);
    }

    public void update(int level, int color) {
        levelIndicator.setVisible(level >= 0);
        levelIndicator.setString(level + "");
        levelIndicator.setColor(color);
    }
}
