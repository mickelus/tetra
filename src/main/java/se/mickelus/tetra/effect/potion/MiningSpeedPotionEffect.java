package se.mickelus.tetra.effect.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.EffectRenderer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.effect.gui.EffectUnRenderer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class MiningSpeedPotionEffect extends MobEffect {
    public static MiningSpeedPotionEffect instance;
    public MiningSpeedPotionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xeeeeee);

        setRegistryName("mining_speed");

        instance = this;
    }



    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getPlayer().hasEffect(instance)) {
            event.setNewSpeed(event.getNewSpeed() * event.getPlayer().getEffect(instance).getAmplifier() / 10f);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<EffectRenderer> consumer) {
        super.initializeClient(consumer);
        consumer.accept(EffectUnRenderer.INSTANCE);
    }
}
