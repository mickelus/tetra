package se.mickelus.tetra.util;

import com.mojang.math.Vector3f;
import net.minecraft.util.FastColor;

import java.util.Arrays;

public class Lherper {
    private static float flip(float x) {
        return 1 - x;
    }

    public static float easeIn(float t) {
        return t * t;
    }

    public static float easeOut(float t) {
        return flip(easeIn(flip(t)));
    }

    public static int lerpColors(float factor, int... colors) {
        Vector3f result = lerpColors(factor, Arrays.stream(colors)
                .mapToObj(color -> new Vector3f(FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color)))
                .toArray(Vector3f[]::new));
        return FastColor.ARGB32.color(255, (int) result.x(), (int) result.y(), (int) result.z());
    }

    public static Vector3f lerpColors(float factor, Vector3f... colors) {
        int size = colors.length;
        float progress = Math.min(1f * factor * (size - 1), size - 1.00001f);
        int index = Math.min((int) progress, size - 2);
        return lerpColors(progress % 1, colors[index], colors[index + 1]);
    }

    private static Vector3f lerpColors(float factor, Vector3f fromColor, Vector3f toColor) {
        Vector3f result = fromColor.copy();
        result.lerp(toColor, factor);
        return result;
    }
}
