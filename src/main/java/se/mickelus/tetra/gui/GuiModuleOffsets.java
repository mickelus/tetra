package se.mickelus.tetra.gui;

public class GuiModuleOffsets {
    private int[] offsetX;
    private int[] offsetY;
    private boolean[] alignment;

    public GuiModuleOffsets(int ... offsets) {
        offsetX = new int[offsets.length / 2];
        offsetY = new int[offsets.length / 2];
        alignment = new boolean[offsets.length / 2];
        for (int i = 0; i < offsets.length / 2; i++) {
            offsetX[i] = offsets[i * 2];
            offsetY[i] = offsets[i * 2 + 1];
            alignment[i] = offsetX[i] > 0;
        }
    }

    public int size() {
        return offsetX.length;
    }

    public int getX(int index) {
        return offsetX[index];
    }

    public int getY(int index) {
        return offsetY[index];
    }

    public boolean getAlignment(int index) {
        return alignment[index];
    }
}
