package se.mickelus.tetra.compat.neoforge.client.gui.overlay;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;

public interface IGuiOverlay extends LayeredDraw.Layer {
    void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight);

    @Override
    default void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        render(
                new ForgeGui(minecraft),
                graphics,
                deltaTracker.getGameTimeDeltaPartialTick(false),
                minecraft.getWindow().getGuiScaledWidth(),
                minecraft.getWindow().getGuiScaledHeight()
        );
    }
}
