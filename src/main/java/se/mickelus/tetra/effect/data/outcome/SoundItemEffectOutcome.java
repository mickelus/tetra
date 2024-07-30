package se.mickelus.tetra.effect.data.outcome;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.number.NumberProvider;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;

public class SoundItemEffectOutcome extends ItemEffectOutcome {
    SoundEvent sound;
    VectorProvider position;
    NumberProvider volume;
    NumberProvider pitch;

    @Override
    public boolean perform(ItemEffectContext context) {
        Vec3 originValue = position.getVector(context);
        context.getLevel().playSound(null, originValue.x(), originValue.y(), originValue.z(), sound, SoundSource.PLAYERS,
                volume != null ? volume.getValue(context) : 1, pitch != null ? pitch.getValue(context) : 1);
        return true;
    }
}
