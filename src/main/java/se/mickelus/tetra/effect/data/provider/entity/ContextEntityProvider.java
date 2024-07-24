package se.mickelus.tetra.effect.data.provider.entity;

import net.minecraft.world.entity.Entity;
import se.mickelus.tetra.effect.data.ItemEffectContext;

public class ContextEntityProvider implements EntityProvider {
    String key;

    public ContextEntityProvider(String key) {
        this.key = key;
    }

    @Override
    public Entity getEntity(ItemEffectContext context) {
        return context.getEntities().get(key);
    }
}
