package se.mickelus.tetra.gui;

public enum GuiAlignment {
    left,
    center,
    right;

    public static GuiAlignment flip(GuiAlignment alignment) {
        if (alignment == left) {
            return right;
        } else if (alignment == right) {
            return left;
        }
        return center;
    }
}
