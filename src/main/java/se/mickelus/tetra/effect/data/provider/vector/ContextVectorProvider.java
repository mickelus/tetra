package se.mickelus.tetra.effect.data.provider.vector;

import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.effect.data.ItemEffectContext;

public class ContextVectorProvider implements VectorProvider {
    String key;

    public ContextVectorProvider(String key) {
        this.key = key;
    }

    @Override
    public Vec3 getVector(ItemEffectContext context) {
        return context.getVectors().get(key);
    }
}
