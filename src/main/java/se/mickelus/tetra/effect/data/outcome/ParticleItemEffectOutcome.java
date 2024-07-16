package se.mickelus.tetra.effect.data.outcome;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.number.NumberProvider;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;

public class ParticleItemEffectOutcome extends ItemEffectOutcome {
    ParticleOptions particle;
    NumberProvider count;
    NumberProvider speed;
    VectorProvider position;
    VectorProvider spread;

    @Override
    public boolean perform(ItemEffectContext context) {
        Vec3 positionValue = position.getVector(context);
        Vec3 spreadValue = spread.getVector(context);
        context.getLevel().sendParticles(particle, positionValue.x(), positionValue.y(), positionValue.z(), count.getIntegerValue(context), spreadValue.x(), spreadValue.y(), spreadValue.z(), speed.getValue(context));
        return true;
    }
}
