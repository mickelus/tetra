package se.mickelus.tetra.effect.potion;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import se.mickelus.mutil.effect.EffectTooltipRenderer;
import se.mickelus.tetra.blocks.ArcaneFireBlock;
import se.mickelus.tetra.effect.CombustingEffect;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class UnstablePowerMobEffect extends MobEffect {
    public static final String identifier = "unstable_power";
    public static UnstablePowerMobEffect instance;
    private static final int splinterTreshhold = 16 * 20;

    public UnstablePowerMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xeeeeee);

        instance = this;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide() && entity.level().getRandom().nextInt(4) == 0) {
            MobEffectInstance current = entity.getEffect(instance);
            if (current.getDuration() >= splinterTreshhold
                    && CombustingEffect.setBlocksAround(entity.level(), entity.blockPosition(), 8, 1, true,
                    ArcaneFireBlock.instance.get().defaultBlockState())) {
                entity.playSound(SoundEvents.ALLAY_HURT, 0.8f, 1.2f);
                if (current.getAmplifier() > 0 && entity.level().getRandom().nextInt(4) == 0) {
                    addOrUpdate(entity, 0, -1);
                } else {
                    addOrUpdate(entity, -splinterTreshhold, 0);
                }
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % (80) == 0;
    }

    public static void addOrUpdate(LivingEntity entity, int duration, int amplifier) {
        MobEffectInstance current = entity.getEffect(instance);
        int currentAmplifier = Optional.ofNullable(current)
                .map(MobEffectInstance::getAmplifier)
                .orElse(0);
        int updatedAmplifier = Math.min(Byte.MAX_VALUE, currentAmplifier + amplifier);

        int currentDuration = Optional.ofNullable(current)
                .map(MobEffectInstance::getDuration)
                .orElse(0);
        int updatedDuration = currentDuration + duration;

        if (updatedDuration < currentDuration || updatedAmplifier < currentAmplifier) {
            entity.removeEffect(instance);
        }

        if (updatedDuration > 0 && updatedAmplifier >= 0) {
            entity.addEffect(new MobEffectInstance(instance, updatedDuration, updatedAmplifier, false, false, true));
        }
    }

    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getEntity().hasEffect(instance)) {
            event.setNewSpeed(event.getNewSpeed() * (1.1f + event.getEntity().getEffect(instance).getAmplifier() * 0.1f));
        }
    }

    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity livingEntity && livingEntity.hasEffect(instance)) {
            event.setAmount(event.getAmount() * (1.1f + livingEntity.getEffect(instance).getAmplifier() * 0.1f));
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
