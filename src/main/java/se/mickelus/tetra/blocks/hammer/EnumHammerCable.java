package se.mickelus.tetra.blocks.hammer;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.util.EnumFacing;

public enum EnumHammerCable {
    EAST1(EnumFacing.EAST, 1, "cablee1"),
    EAST2(EnumFacing.EAST, 2, "cablee2"),
    EAST3(EnumFacing.EAST, 3, "cablee3"),
    EAST4(EnumFacing.EAST, 4, "cablee4"),
    WEST1(EnumFacing.WEST, 1, "cablew1"),
    WEST2(EnumFacing.WEST, 2, "cablew2"),
    WEST3(EnumFacing.WEST, 3, "cablew3"),
    WEST4(EnumFacing.WEST, 4, "cablew4");

    public EnumFacing face;
    public int index;
    public PropertyBool prop;
    public String key;

    EnumHammerCable(EnumFacing face, int index, String key) {
        this.face = face;
        this.index = index;
        this.key = key;
        this.prop = PropertyBool.create(key);
    }
}
