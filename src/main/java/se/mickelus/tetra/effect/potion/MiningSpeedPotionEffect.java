package se.mickelus.tetra.effect.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MiningSpeedPotionEffect extends MobEffect {
    public static MiningSpeedPotionEffect instance;
    public MiningSpeedPotionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xeeeeee);

        setRegistryName("mining_speed");

        instance = this;
    }

    @Override
    public boolean shouldRender(MobEffectInstance effect) {
        return false;
    }

    @Override
    public boolean shouldRenderHUD(MobEffectInstance effect) {
        return false;
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getPlayer().hasEffect(instance)) {
            event.setNewSpeed(event.getNewSpeed() * event.getPlayer().getEffect(instance).getAmplifier() / 10f);
        }
    }
}
