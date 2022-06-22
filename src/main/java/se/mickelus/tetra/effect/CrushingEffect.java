package se.mickelus.tetra.effect;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import se.mickelus.mutil.util.CastOptional;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class CrushingEffect {
    public static void onLivingDamage(LivingDamageEvent event, int effectLevel) {
        if (effectLevel > 0 && event.getAmount() < effectLevel) {
            float attackStrength = CastOptional.cast(event.getSource().getDirectEntity(), Player.class)
                    .map(EffectHelper::getCooledAttackStrength)
                    .orElse(1f);
            if (attackStrength > 0.9) {
                event.setAmount(effectLevel);
            }
        }
    }
}
