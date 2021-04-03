package se.mickelus.tetra.effect;

import net.minecraftforge.event.entity.living.LivingDamageEvent;

public class CrushingEffect {

    public static void onLivingDamage(LivingDamageEvent event, int effectLevel) {
        if (effectLevel > 0 && event.getAmount() < effectLevel) {
            event.setAmount(effectLevel);
        }
    }
}
