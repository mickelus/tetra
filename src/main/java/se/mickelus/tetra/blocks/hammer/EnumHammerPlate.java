package se.mickelus.tetra.blocks.hammer;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.util.EnumFacing;

public enum EnumHammerPlate {
    EAST(EnumFacing.EAST, "plate1"),
    WEST(EnumFacing.WEST, "plate2");

    public EnumFacing face;
    public PropertyBool prop;
    public String key;

    EnumHammerPlate(EnumFacing face, String key) {
        this.face = face;
        this.key = key;
        this.prop = PropertyBool.create(key);
    }
}
