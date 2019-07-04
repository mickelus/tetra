package se.mickelus.tetra.blocks.forged.transfer;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

public enum EnumTransferConfig implements IStringSerializable {

    A,
    B,
    C;

    public static final PropertyEnum<EnumTransferConfig> prop = PropertyEnum.create("config", EnumTransferConfig.class);

    @Override
    public String getName() {
        return toString().toLowerCase();
    }

    public static EnumTransferConfig getNextConfiguration(EnumTransferConfig config) {
        int index = ( config.ordinal() + 1 ) % values().length;
        return values()[index];
    }
}
