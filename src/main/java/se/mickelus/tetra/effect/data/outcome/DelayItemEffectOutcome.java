package se.mickelus.tetra.effect.data.outcome;

import net.minecraft.world.entity.Entity;
import se.mickelus.tetra.ServerScheduler;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.condition.FixedItemEffectCondition;
import se.mickelus.tetra.effect.data.condition.ItemEffectCondition;
import se.mickelus.tetra.effect.data.provider.entity.EntityProvider;
import se.mickelus.tetra.effect.data.provider.number.NumberProvider;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DelayItemEffectOutcome extends ItemEffectOutcome {
    ItemEffectOutcome outcome;
    NumberProvider delay;
    String key;
    EntityProvider keyEntity;

    ItemEffectCondition replaceKey = new FixedItemEffectCondition(true);

    @Override
    public boolean perform(ItemEffectContext context) {
        if (key != null || keyEntity != null) {
            String entityId = Optional.ofNullable(keyEntity)
                    .map(provider -> provider.getEntity(context))
                    .map(Entity::getStringUUID)
                    .orElse(null);
            String concatenatedKey = Stream.of(key, entityId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(""));

            if (replaceKey.test(context) || !ServerScheduler.isScheduled(concatenatedKey)) {
                ServerScheduler.schedule(concatenatedKey, delay.getIntegerValue(context), () -> outcome.perform(context));
                return true;
            }
            return false;
        }
        
        ServerScheduler.schedule(delay.getIntegerValue(context), () -> outcome.perform(context));
        return true;
    }
}
