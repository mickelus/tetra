package se.mickelus.tetra.blocks.forged.transfer;

public enum EnumTransferEffect {
    send,
    receive,
    redstone;

    public static EnumTransferEffect fromConfig(EnumTransferConfig config, long seed) {
        return EnumTransferEffect.values()[config.ordinal() % EnumTransferEffect.values().length];
    }
}
