package se.mickelus.tetra.blocks.forged.transfer;

public enum EnumTransferEffect {

    SEND,
    RECEIVE,
    REDSTONE;


    public static EnumTransferEffect fromConfig(EnumTransferConfig config, long seed) {
        return EnumTransferEffect.values()[config.ordinal() % EnumTransferEffect.values().length];
    }
}
