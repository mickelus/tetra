package se.mickelus.tetra;

import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;

public class RotationHelper {
    public static Rotation rotationFromFacing(Direction facing) {
        switch (facing) {
            case UP:
            case DOWN:
            case NORTH:
                return Rotation.NONE;
            case SOUTH:
                return Rotation.CLOCKWISE_180;
            case EAST:
                return Rotation.CLOCKWISE_90;
            case WEST:
                return Rotation.COUNTERCLOCKWISE_90;
            default:
                return Rotation.NONE;
        }
    }
}
