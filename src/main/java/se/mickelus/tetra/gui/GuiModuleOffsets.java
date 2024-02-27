package se.mickelus.tetra.gui;

import se.mickelus.tetra.items.modular.impl.dynamic.ArchetypeSlotDefinition;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class GuiModuleOffsets {
    private final int[] offsetX;
    private final int[] offsetY;
    private final boolean[] alignment;

    public GuiModuleOffsets(int... offsets) {
        offsetX = new int[offsets.length / 2];
        offsetY = new int[offsets.length / 2];
        alignment = new boolean[offsets.length / 2];
        for (int i = 0; i < offsets.length / 2; i++) {
            offsetX[i] = offsets[i * 2];
            offsetY[i] = offsets[i * 2 + 1];
            alignment[i] = offsetX[i] > 0;
        }
    }

    public GuiModuleOffsets(ArchetypeSlotDefinition[] slots) {
        offsetX = new int[slots.length];
        offsetY = new int[slots.length];
        alignment = new boolean[slots.length];
        for (int i = 0; i < slots.length; i++) {
            offsetX[i] = slots[i].x();
            offsetY[i] = slots[i].y();
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
