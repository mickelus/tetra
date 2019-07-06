package se.mickelus.tetra.blocks.forged.transfer;

import se.mickelus.tetra.blocks.hammer.EnumHammerConfig;

public enum EnumTransferEffect {

    SEND,
    RECEIVE,
    REDSTONE;


    public static EnumTransferEffect fromConfig(EnumHammerConfig config, long seed) {
        return EnumTransferEffect.values()[config.ordinal() % EnumTransferEffect.values().length];
    }
}
