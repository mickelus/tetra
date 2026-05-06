package se.mickelus.tetra.effect.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SatiatedPotionEffect extends MobEffect {
    public static final String identifier = "satiated";
    public static SatiatedPotionEffect instance;

    public SatiatedPotionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xffffff);

        instance = this;
    }
}
