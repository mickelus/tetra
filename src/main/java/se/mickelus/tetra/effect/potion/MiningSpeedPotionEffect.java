package se.mickelus.tetra.effect.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class MiningSpeedPotionEffect extends MobEffect {
    public static final String identifier = "mining_speed";
    public static MiningSpeedPotionEffect instance;

    public MiningSpeedPotionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xeeeeee);

        instance = this;
    }


    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        var effect = se.mickelus.tetra.effect.EffectHelper.effectHolder(instance);
        if (event.getEntity().hasEffect(effect)) {
            event.setNewSpeed(event.getNewSpeed() * (1 + event.getEntity().getEffect(effect).getAmplifier() / 10f));
        }
    }
}
