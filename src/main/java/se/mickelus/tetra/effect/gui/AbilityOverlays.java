package se.mickelus.tetra.effect.gui;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.mutil.gui.GuiRoot;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class AbilityOverlays extends GuiRoot implements IGuiOverlay {
    public static AbilityOverlays instance;

    private final ChargeBarGui chargeBar;
    private final ComboPointGui comboPoints;
    private final RevengeGui revengeIndicator;
    private final FocusGui focusIndicator;

    public AbilityOverlays(Minecraft mc) {
        super(mc);

        chargeBar = new ChargeBarGui();
        addChild(chargeBar);

        comboPoints = new ComboPointGui();
        addChild(comboPoints);

        revengeIndicator = new RevengeGui();
        addChild(revengeIndicator);

        focusIndicator = new FocusGui();
        addChild(focusIndicator);

        instance = this;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (TickEvent.Phase.START == event.phase && mc.player != null) {
            chargeBar.update(mc.player);
            comboPoints.update(mc.player);
            revengeIndicator.update(mc.player, mc.hitResult);
            focusIndicator.update(mc.player);
        }
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (isVisible()) {
            Window window = mc.getWindow();
            int width = window.getGuiScaledWidth();
            int height = window.getGuiScaledHeight();

            this.drawChildren(guiGraphics, Math.round(width / 2f), Math.round(height / 2f), 0, 0, 0, 0, 1f);
        }
    }
}
