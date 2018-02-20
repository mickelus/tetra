package se.mickelus.tetra;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;

public class PotionBleeding extends Potion {
    public static PotionBleeding instance;
    protected PotionBleeding() {
        super(true, 0);

        setRegistryName("bleeding");

        instance = this;
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        entity.attackEntityFrom(DamageSource.GENERIC, amplifier);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration % 10 == 0;
    }
}
