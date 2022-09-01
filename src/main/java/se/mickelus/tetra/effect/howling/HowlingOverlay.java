package se.mickelus.tetra.effect.howling;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class HowlingOverlay implements IGuiOverlay {
    private final Minecraft mc;

    private final HowlingProgressGui gui;

    public HowlingOverlay(Minecraft mc) {
        this.mc = mc;

        gui = new HowlingProgressGui(mc);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (TickEvent.Phase.START == event.phase) {
            int amplifier = Optional.ofNullable(mc.player)
                    .map(player -> player.getEffect(HowlingPotionEffect.instance))
                    .map(MobEffectInstance::getAmplifier)
                    .orElse(-1);

            gui.updateAmplifier(amplifier);
        }
    }

    @Override
    public void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        this.gui.draw(poseStack);
    }
}
