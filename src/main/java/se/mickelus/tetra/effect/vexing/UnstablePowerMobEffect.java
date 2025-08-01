package se.mickelus.tetra.effect.vexing;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import se.mickelus.mutil.effect.EffectTooltipRenderer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class UnstablePowerMobEffect extends MobEffect {
    public static final String identifier = "unstable_power";
    public static UnstablePowerMobEffect instance;

    public UnstablePowerMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xeeeeee);

        instance = this;
    }

    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getEntity().hasEffect(UnstablePowerMobEffect.instance)) {
            event.setNewSpeed(event.getNewSpeed() * (1.1f + event.getEntity().getEffect(UnstablePowerMobEffect.instance).getAmplifier() * 0.1f));
        }
    }

    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity livingEntity && livingEntity.hasEffect(UnstablePowerMobEffect.instance)) {
            event.setAmount(event.getAmount() * (1.1f + livingEntity.getEffect(UnstablePowerMobEffect.instance).getAmplifier() * 0.1f));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new EffectTooltipRenderer(effect -> I18n.get("effect.tetra.unstable_power.tooltip",
                (effect.getAmplifier() + 1) * 10)));
    }
}
