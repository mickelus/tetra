package se.mickelus.tetra.blocks.forged.transfer;

import net.minecraft.util.StringRepresentable;

public enum EnumTransferConfig implements StringRepresentable {
    a,
    b,
    c;

    public static EnumTransferConfig getNextConfiguration(EnumTransferConfig config) {
        int index = (config.ordinal() + 1) % values().length;
        return values()[index];
    }

    @Override
    public String getSerializedName() {
        return toString().toLowerCase();
    }
}
