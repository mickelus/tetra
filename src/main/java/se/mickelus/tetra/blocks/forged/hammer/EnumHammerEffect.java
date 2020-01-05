package se.mickelus.tetra.blocks.forged.hammer;

public enum EnumHammerEffect {

    EFFICIENT(false),
    OVERCHARGED(true),
    LEAKY(false),
    DAMAGING(false);

    public boolean requiresBoth;

    EnumHammerEffect(boolean requiresBoth) {
        this.requiresBoth = requiresBoth;
    }

    public static EnumHammerEffect fromConfig(EnumHammerConfig config, long seed) {
        return EnumHammerEffect.values()[config.ordinal() % EnumHammerEffect.values().length];
    }
}
