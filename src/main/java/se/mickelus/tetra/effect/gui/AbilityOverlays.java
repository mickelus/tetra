package se.mickelus.tetra.effect.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.mgui.gui.GuiRoot;

public class AbilityOverlays extends GuiRoot {
    public static AbilityOverlays instance;

    private ChargeBarGui chargeBar;
    private ComboPointGui comboPoints;
    private RevengeGui revengeIndicator;

    public AbilityOverlays(Minecraft mc) {
        super(mc);

        chargeBar = new ChargeBarGui();
        addChild(chargeBar);

        comboPoints = new ComboPointGui();
        addChild(comboPoints);

        revengeIndicator = new RevengeGui();
        addChild(revengeIndicator);

        instance = this;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            return;
        }

        chargeBar.update(mc.player);
        comboPoints.update(mc.player);
        revengeIndicator.update(mc.player, mc.objectMouseOver);

        draw(event.getMatrixStack());
    }

    public void draw(MatrixStack matrixStack) {
        if (isVisible()) {
            MainWindow window = mc.getMainWindow();
            int width = window.getScaledWidth();
            int height = window.getScaledHeight();

            this.drawChildren(matrixStack, width / 2, height / 2, 0, 0, 0, 0, 1.0F);
        }
    }
}
