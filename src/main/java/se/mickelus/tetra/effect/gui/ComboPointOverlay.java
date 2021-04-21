package se.mickelus.tetra.effect.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.effect.ChargedAbilityEffect;
import se.mickelus.tetra.effect.ComboPoints;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.util.CastOptional;

import java.util.Optional;

public class ComboPointOverlay {
    public static ComboPointOverlay instance;

    private final Minecraft mc;

    private ComboPointGui gui;

    public ComboPointOverlay(Minecraft mc) {
        this.mc = mc;

        gui = new ComboPointGui(mc);

        instance = this;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            return;
        }

        if (ComboPoints.canSpend(mc.player)) {
            gui.update(ComboPoints.get(mc.player));
        } else {
            gui.update(0);
        }

        gui.draw(event.getMatrixStack());
    }
}
