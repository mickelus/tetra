package se.mickelus.tetra.blocks.forged.hammer;

import net.minecraft.state.EnumProperty;
import net.minecraft.util.IStringSerializable;

public enum EnumHammerConfig implements IStringSerializable {

    A,
    B,
    C,
    D;

    public static final EnumProperty<EnumHammerConfig> eastProp = EnumProperty.create("confige", EnumHammerConfig.class);
    public static final EnumProperty<EnumHammerConfig> westProp = EnumProperty.create("configw", EnumHammerConfig.class);

    @Override
    public String getString() {
        return toString().toLowerCase();
    }

    public static EnumHammerConfig getNextConfiguration(EnumHammerConfig config) {
        int index = ( config.ordinal() + 1 ) % values().length;
        return values()[index];
    }
}
