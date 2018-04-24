package se.mickelus.tetra.gui;

public enum GuiAlignment {
    left,
    center,
    right;

    public GuiAlignment flip() {
        if (this == left) {
            return right;
        } else if (this == right) {
            return left;
        }
        return center;
    }

    public GuiAttachment toAttachment() {
        if (this == left) {
            return GuiAttachment.topLeft;
        } else if (this == right) {
            return GuiAttachment.topRight;
        }
        return GuiAttachment.topCenter;
    }
}
