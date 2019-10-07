package se.mickelus.tetra.blocks.forged.transfer;

import net.minecraft.state.EnumProperty;
import net.minecraft.util.IStringSerializable;

public enum EnumTransferConfig implements IStringSerializable {

    A,
    B,
    C;

    public static final EnumProperty<EnumTransferConfig> prop = EnumProperty.create("config", EnumTransferConfig.class);

    @Override
    public String getName() {
        return toString().toLowerCase();
    }

    public static EnumTransferConfig getNextConfiguration(EnumTransferConfig config) {
        int index = ( config.ordinal() + 1 ) % values().length;
        return values()[index];
    }
}
