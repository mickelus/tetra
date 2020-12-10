package se.mickelus.tetra.util;

import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

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

    public static BlockPos rotatePitch(BlockPos pos, float pitch) {
        float f = MathHelper.cos(pitch);
        float f1 = MathHelper.sin(pitch);
        float x = pos.getX();
        float y = pos.getY() * f + pos.getZ() * f1;
        float z = pos.getZ() * f - pos.getY() * f1;
        return new BlockPos(Math.round(x), Math.round(y), Math.round(z));
    }

    public static BlockPos rotateYaw(BlockPos pos, float yaw) {
        float f = MathHelper.cos(yaw);
        float f1 = MathHelper.sin(yaw);
        double x = pos.getX() * (double)f + pos.getZ() * (double)f1;
        double y = pos.getY();
        double z = pos.getZ() * (double)f - pos.getX() * (double)f1;
        return new BlockPos(x, y, z);
    }

    public static BlockPos rotateDirection(BlockPos pos, Direction facing) {
        switch (facing) {
            default:
            case SOUTH:
                return pos;
            case WEST:
                return new BlockPos(-pos.getZ(), pos.getY(), pos.getX());
            case NORTH:
                return new BlockPos(-pos.getX(), pos.getY(), -pos.getZ());
            case EAST:
                return new BlockPos(pos.getZ(), pos.getY(), -pos.getX());
        }
    }
}
