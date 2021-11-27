package se.mickelus.tetra.util;

import net.minecraft.block.Block;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

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

    // todo: there has to be a less hacky way?
    public static VoxelShape rotateDirection(VoxelShape shape, Direction facing) {
        VoxelShape[] temp = new VoxelShape[] { shape.withOffset(-0.5, 0, -0.5), VoxelShapes.empty() };

        for (int i = 0; i < facing.getHorizontalIndex(); i++) {
            temp[0].forEachBox((x1, y1, z1, x2, y2, z2) -> temp[1] = VoxelShapes.or(temp[1], VoxelShapes.create(-z1, y1, x1, -z2, y2, x2)));
            temp[0] = temp[1];
            temp[1] = VoxelShapes.empty();
        }

        return temp[0].withOffset(0.5, 0, 0.5);
    }

    public static Vector3i shiftAxis(Vector3i pos) {
        return new Vector3i(pos.getY(), pos.getZ(), pos.getX());
    }

    /**
     * Returns the horizontal angle between two points, in radians (is that how you say it?)
     * @param a
     * @param b
     * @return
     */
    public static double getHorizontalAngle(Vector3d a, Vector3d b) {
        return MathHelper.atan2(a.x - b.x, a.z - b.z);
    }
}
