package se.mickelus.tetra.blocks.hammer;

import net.minecraft.state.BooleanProperty;
import net.minecraft.util.Direction;

public enum EnumHammerPlate {
    EAST(Direction.EAST, "platee"),
    WEST(Direction.WEST, "platew");

    public Direction face;
    public PropertyBool prop;
    public String key;

    EnumHammerPlate(Direction face, String key) {
        this.face = face;
        this.key = key;
        this.prop = PropertyBool.create(key);
    }
}
