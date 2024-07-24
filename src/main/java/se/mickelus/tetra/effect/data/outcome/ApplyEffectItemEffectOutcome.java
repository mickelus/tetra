package se.mickelus.tetra.effect.data.outcome;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.entity.EntityProvider;
import se.mickelus.tetra.effect.data.provider.number.NumberProvider;

public class ApplyEffectItemEffectOutcome extends ItemEffectOutcome {
    MobEffect effect;
    NumberProvider duration;
    NumberProvider amplifier;
    EntityProvider entity;
    EntityProvider source;
    NumberProvider stackDurationCap;

    @Override
    public boolean perform(ItemEffectContext context) {
        LivingEntity entity = CastOptional.cast(this.entity.getEntity(context), LivingEntity.class).orElse(null);
        if (entity != null) {
            int duration = this.duration.getIntegerValue(context);
            int amplifier = this.amplifier.getIntegerValue(context);
            int targetDuration = stackDurationCap != null && entity.hasEffect(effect)
                    ? Math.min(duration + entity.getEffect(effect).getDuration(), stackDurationCap.getIntegerValue(context))
                    : duration;
            return entity.addEffect(new MobEffectInstance(effect, targetDuration, amplifier), source != null ? source.getEntity(context) : null);
        }
        return false;
    }
}
