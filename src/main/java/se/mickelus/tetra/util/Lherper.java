package se.mickelus.tetra.util;

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
}
