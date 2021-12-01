package se.mickelus.tetra.effect.potion;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MiningSpeedPotionEffect extends Effect {
    public static MiningSpeedPotionEffect instance;
    public MiningSpeedPotionEffect() {
        super(EffectType.BENEFICIAL, 0xeeeeee);

        setRegistryName("mining_speed");

        instance = this;
    }

    @Override
    public boolean shouldRender(EffectInstance effect) {
        return false;
    }

    @Override
    public boolean shouldRenderHUD(EffectInstance effect) {
        return false;
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getPlayer().hasEffect(instance)) {
            event.setNewSpeed(event.getNewSpeed() * event.getPlayer().getEffect(instance).getAmplifier() / 10f);
        }
    }
}
