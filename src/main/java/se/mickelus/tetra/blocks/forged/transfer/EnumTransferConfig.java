package se.mickelus.tetra.blocks.forged.transfer;

import net.minecraft.util.IStringSerializable;

public enum EnumTransferConfig implements IStringSerializable {
    a,
    b,
    c;

    @Override
    public String getName() {
        return toString().toLowerCase();
    }

    public static EnumTransferConfig getNextConfiguration(EnumTransferConfig config) {
        int index = ( config.ordinal() + 1 ) % values().length;
        return values()[index];
    }
}
