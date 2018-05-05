package se.mickelus.tetra.items.toolbelt.booster;

import net.minecraft.client.Minecraft;
import se.mickelus.tetra.gui.GuiRoot;

public class OverlayGuiBooster extends GuiRoot {

    private GuiBarBooster barElement;

    public OverlayGuiBooster(Minecraft mc) {
        super(mc);

        barElement = new GuiBarBooster(50, 100, 0, 0);
        addChild(barElement);
    }

    public void setFuel(float fuel) {
        barElement.setFuel(fuel);
    }
}
