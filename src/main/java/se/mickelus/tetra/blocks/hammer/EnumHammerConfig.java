package se.mickelus.tetra.blocks.hammer;

import net.minecraft.state.EnumProperty;
import net.minecraft.util.IStringSerializable;

public enum EnumHammerConfig implements IStringSerializable {

    A,
    B,
    C,
    D;

    public static final EnumProperty<EnumHammerConfig> propE = EnumProperty.create("confige", EnumHammerConfig.class);
    public static final EnumProperty<EnumHammerConfig> propW = EnumProperty.create("configw", EnumHammerConfig.class);

    @Override
    public String getName() {
        return toString().toLowerCase();
    }

    public static EnumHammerConfig getNextConfiguration(EnumHammerConfig config) {
        int index = ( config.ordinal() + 1 ) % values().length;
        return values()[index];
    }
}
