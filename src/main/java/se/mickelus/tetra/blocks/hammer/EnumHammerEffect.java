package se.mickelus.tetra.blocks.hammer;

public enum EnumHammerEffect {

    EFFICIENT(false),
    SEALABLE(false),
    OVERCHARGED(true),
    LEAKY(false),
    DAMAGING(false);

    public boolean requiresBoth;

    EnumHammerEffect(boolean requiresBoth) {
        this.requiresBoth = requiresBoth;
    }

    public static EnumHammerEffect fromConfig(EnumHammerConfig config, long seed) {
        if (EnumHammerConfig.A.equals(config)) {
            return null;
        }

        return EnumHammerEffect.values()[(int)( config.ordinal() + seed ) % EnumHammerEffect.values().length];
    }
}
