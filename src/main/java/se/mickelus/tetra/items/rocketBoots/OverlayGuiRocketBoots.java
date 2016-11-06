package se.mickelus.tetra.items.rocketBoots;

import net.minecraft.client.Minecraft;
import se.mickelus.tetra.gui.GuiRoot;

public class OverlayGuiRocketBoots extends GuiRoot {

    private GuiBarRocketBoots barElement;

    public OverlayGuiRocketBoots(Minecraft mc) {
        super(mc);

        barElement = new GuiBarRocketBoots(50, 100, 0, 0);
        addChild(barElement);
    }

    public void setFuel(float fuel) {
        barElement.setFuel(fuel);
    }
}
