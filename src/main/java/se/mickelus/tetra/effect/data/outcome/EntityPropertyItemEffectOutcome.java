package se.mickelus.tetra.effect.data.outcome;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.entity.EntityProvider;
import se.mickelus.tetra.effect.data.provider.number.NumberProvider;

public class EntityPropertyItemEffectOutcome extends ItemEffectOutcome {
    EntityProvider entity;
    NumberProvider value;
    EntityProperty property;

    @Override
    public boolean perform(ItemEffectContext context) {
        Entity resolvedEntity = entity.getEntity(context);
        switch (property) {
            case freezingTicks:
                resolvedEntity.setTicksFrozen((int) value.getValue(context));
                return true;
            case burningDuration:
                resolvedEntity.setRemainingFireTicks((int) value.getValue(context));
                return true;
            case airSupply:
                resolvedEntity.setAirSupply((int) value.getValue(context));
                return true;
            case health:
                if (resolvedEntity instanceof LivingEntity livingEntity) {
                    livingEntity.setHealth(value.getValue(context));
                    return true;
                }
                break;
            case absorption:
                if (resolvedEntity instanceof LivingEntity livingEntity) {
                    livingEntity.setAbsorptionAmount(value.getValue(context));
                    return true;
                }
                break;
        }
        return false;
    }

    enum EntityProperty {
        freezingTicks,
        burningDuration,
        airSupply,
        health,
        absorption,
    }
}
