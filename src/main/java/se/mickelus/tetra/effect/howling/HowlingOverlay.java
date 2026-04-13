package se.mickelus.tetra.effect.howling;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class HowlingOverlay implements LayeredDraw.Layer {
    private final Minecraft mc;

    private final HowlingProgressGui gui;

    public HowlingOverlay(Minecraft mc) {
        this.mc = mc;

        gui = new HowlingProgressGui(mc);
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Pre event) {
        int amplifier = Optional.ofNullable(mc.player)
                .map(player -> player.getEffect(se.mickelus.tetra.effect.EffectHelper.effectHolder(HowlingPotionEffect.instance)))
                .map(MobEffectInstance::getAmplifier)
                .orElse(-1);

        gui.updateAmplifier(amplifier);
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        this.gui.draw(graphics);
    }
}
