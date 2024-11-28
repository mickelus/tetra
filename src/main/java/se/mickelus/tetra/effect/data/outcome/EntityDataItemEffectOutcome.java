package se.mickelus.tetra.effect.data.outcome;

import net.minecraft.world.entity.Entity;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.entity.EntityProvider;
import se.mickelus.tetra.effect.data.provider.number.NumberProvider;

public class EntityDataItemEffectOutcome extends ItemEffectOutcome {
    EntityProvider entity;
    NumberProvider value;
    String key;

    @Override
    public boolean perform(ItemEffectContext context) {
        Entity resolvedEntity = entity.getEntity(context);
        resolvedEntity.getPersistentData().putFloat(key, value.getValue(context));
        return true;
    }
}
