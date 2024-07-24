package se.mickelus.tetra.effect.data.condition;

import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.entity.EntityProvider;

public class EntitiesEqualsItemEffectCondition extends ItemEffectCondition {
    EntityProvider a;
    EntityProvider b;

    @Override
    public boolean test(ItemEffectContext context) {
        return a.getEntity(context).equals(b.getEntity(context));
    }
}
