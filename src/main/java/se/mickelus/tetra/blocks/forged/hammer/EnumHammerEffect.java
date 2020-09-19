package se.mickelus.tetra.blocks.forged.hammer;

public enum EnumHammerEffect {

    efficient,
    power,
    precise,
    reliable;


    public static EnumHammerEffect fromConfig(EnumHammerConfig config, long seed) {
        return EnumHammerEffect.values()[config.ordinal() % EnumHammerEffect.values().length];
    }
}
