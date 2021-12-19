package se.mickelus.tetra.effect.howling;

import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class HowlingOverlay {
    public static HowlingOverlay instance;

    private final Minecraft mc;

    private final HowlingProgressGui gui;

    public HowlingOverlay(Minecraft mc) {
        this.mc = mc;

        gui = new HowlingProgressGui(mc);

        instance = this;
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        int amplifier = Optional.ofNullable(mc.player)
                .map(player -> player.getEffect(HowlingPotionEffect.instance))
                .map(MobEffectInstance::getAmplifier)
                .orElse(-1);

        gui.updateAmplifier(amplifier);

        gui.draw(event.getMatrixStack());
    }
}
