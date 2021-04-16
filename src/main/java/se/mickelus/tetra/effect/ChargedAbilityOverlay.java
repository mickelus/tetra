package se.mickelus.tetra.effect;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.util.CastOptional;

import java.util.Optional;

public class ChargedAbilityOverlay {
    public static ChargedAbilityOverlay instance;

    private final Minecraft mc;

    private ChargedAbilityGui gui;

    public ChargedAbilityOverlay(Minecraft mc) {
        this.mc = mc;

        gui = new ChargedAbilityGui(mc);

        instance = this;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            return;
        }

        ItemStack activeStack = mc.player.getActiveItemStack();
        ItemModularHandheld item = CastOptional.cast(activeStack.getItem(), ItemModularHandheld.class).orElse(null);
        ChargedAbilityEffect ability = Optional.ofNullable(item).map(i -> i.getChargeableAbility(activeStack)).orElse(null);

        if (ability != null) {
            gui.setProgress(getProgress(mc.player, item, activeStack, ability), ability.canOvercharge(item, activeStack));
        } else {
            gui.setProgress(0, false);
        }

        gui.draw(event.getMatrixStack());
    }

    private float getProgress(PlayerEntity player, ItemModularHandheld item, ItemStack itemStack, ChargedAbilityEffect ability) {
        return ability != null ? (itemStack.getUseDuration() - player.getItemInUseCount()) * 1f / ability.getChargeTime(item, itemStack) : 0;
    }
}
