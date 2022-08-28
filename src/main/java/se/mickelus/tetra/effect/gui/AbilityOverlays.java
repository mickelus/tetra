package se.mickelus.tetra.effect.gui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.mutil.gui.GuiRoot;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class AbilityOverlays extends GuiRoot {
    public static AbilityOverlays instance;

    private final ChargeBarGui chargeBar;
    private final ComboPointGui comboPoints;
    private final RevengeGui revengeIndicator;

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
    public void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        chargeBar.update(mc.player);
        comboPoints.update(mc.player);
        revengeIndicator.update(mc.player, mc.hitResult);

        draw(event.getPoseStack());
    }

    public void draw(PoseStack matrixStack) {
        if (isVisible()) {
            Window window = mc.getWindow();
            int width = window.getGuiScaledWidth();
            int height = window.getGuiScaledHeight();

            this.drawChildren(matrixStack, width / 2, height / 2, 0, 0, 0, 0, 1.0F);
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
    }
}
