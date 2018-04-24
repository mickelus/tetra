package se.mickelus.tetra.gui;

public enum GuiAttachment {
    topLeft,
    topCenter,
    topRight,
    middleLeft,
    middleCenter,
    middleRight,
    bottomLeft,
    bottomCenter,
    bottomRight;

    public GuiAttachment flipHorizontal() {
        switch(this) {
            case topLeft:
                return topRight;
            case topRight:
                return topLeft;
            case middleLeft:
                return middleRight;
            case middleRight:
                return middleLeft;
            case bottomLeft:
                return bottomRight;
            case bottomRight:
                return bottomLeft;
            default:
                return this;
        }
    }
}
