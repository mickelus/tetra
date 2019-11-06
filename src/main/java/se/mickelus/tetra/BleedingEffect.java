package se.mickelus.tetra;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;

public class BleedingEffect extends Effect {
    public static BleedingEffect instance;
    protected BleedingEffect() {
        super(EffectType.HARMFUL, 0x880000);

        setRegistryName("bleeding");

        instance = this;
    }

    @Override
    public boolean shouldRender(EffectInstance effect) {
        return false;
    }

    @Override
    public boolean shouldRenderHUD(EffectInstance effect) {
        return false;
    }
}
