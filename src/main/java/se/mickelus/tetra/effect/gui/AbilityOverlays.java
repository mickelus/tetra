package se.mickelus.tetra.effect.gui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
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
        revengeIndicator.update(mc.player, mc.hitResult);

        draw(event.getMatrixStack());
    }

    public void draw(PoseStack matrixStack) {
        if (isVisible()) {
            Window window = mc.getWindow();
            int width = window.getGuiScaledWidth();
            int height = window.getGuiScaledHeight();

            this.drawChildren(matrixStack, width / 2, height / 2, 0, 0, 0, 0, 1.0F);
        }
    }
}
