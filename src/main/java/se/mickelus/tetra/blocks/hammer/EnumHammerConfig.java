package se.mickelus.tetra.blocks.hammer;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

public enum EnumHammerConfig implements IStringSerializable {

    A,
    B,
    C,
    D;

    public static final PropertyEnum<EnumHammerConfig> propE = PropertyEnum.create("confige", EnumHammerConfig.class);
    public static final PropertyEnum<EnumHammerConfig> propW = PropertyEnum.create("configw", EnumHammerConfig.class);

    @Override
    public String getName() {
        return toString().toLowerCase();
    }

    public static EnumHammerConfig getNextConfiguration(EnumHammerConfig config) {
        int index = ( config.ordinal() + 1 ) % values().length;
        return values()[index];
    }
}
