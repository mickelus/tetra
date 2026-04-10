package se.mickelus.tetra.compat.neoforge.client.gui.overlay;

import net.minecraft.client.Minecraft;

public final class ForgeGui {
    private final Minecraft minecraft;

    public ForgeGui(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public Minecraft getMinecraft() {
        return minecraft;
    }
}
