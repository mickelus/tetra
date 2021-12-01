package se.mickelus.tetra.blocks.forged.transfer;

import net.minecraft.util.StringRepresentable;

public enum EnumTransferState implements StringRepresentable {
    sending,
    receiving,
    none;

    @Override
    public String getSerializedName() {
        return toString().toLowerCase();
    }
}
