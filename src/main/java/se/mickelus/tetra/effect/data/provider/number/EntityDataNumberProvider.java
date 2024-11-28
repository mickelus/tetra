package se.mickelus.tetra.effect.data.provider.number;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.entity.EntityProvider;

public class EntityDataNumberProvider implements NumberProvider {
    EntityProvider entity;
    String key;
    int defaultValue = -1;

    @Override
    public float getValue(ItemEffectContext context) {
        CompoundTag data = entity.getEntity(context).getPersistentData();
        if (data.contains(key, Tag.TAG_ANY_NUMERIC)) {
            return data.getFloat(key);
        }
        return defaultValue;
    }
}
