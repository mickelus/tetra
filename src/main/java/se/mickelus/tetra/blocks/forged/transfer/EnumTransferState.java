package se.mickelus.tetra.blocks.forged.transfer;

import net.minecraft.util.IStringSerializable;

public enum EnumTransferState implements IStringSerializable {
    sending,
    receiving,
    none;

    @Override
    public String getString() {
        return toString().toLowerCase();
    }
}
