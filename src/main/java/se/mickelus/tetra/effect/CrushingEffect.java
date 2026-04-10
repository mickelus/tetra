package se.mickelus.tetra.effect;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import se.mickelus.mutil.util.CastOptional;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class CrushingEffect {
    public static void onLivingDamage(LivingDamageEvent.Pre event, int effectLevel) {
        if (effectLevel > 0 && event.getNewDamage() < effectLevel) {
            float attackStrength = CastOptional.cast(event.getSource().getDirectEntity(), Player.class)
                    .map(EffectHelper::getCooledAttackStrength)
                    .orElse(1f);
            if (attackStrength > 0.9) {
                event.setNewDamage(effectLevel);
            }
        }
    }
}
