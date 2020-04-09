package se.mickelus.tetra.gui;

import se.mickelus.tetra.items.modular.ItemModular;
import se.mickelus.tetra.items.modular.impl.ModularDoubleHeadedItem;
import se.mickelus.tetra.items.modular.impl.ModularSingleHeadedItem;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;
import se.mickelus.tetra.items.modular.impl.toolbelt.ModularToolbeltItem;

public class GuiModuleOffsets {
    static GuiModuleOffsets[] defaultMajorOffsets = {
            new GuiModuleOffsets(4, 0),
            new GuiModuleOffsets(4, 0, 4, 18),
            new GuiModuleOffsets(4, 0, 4, 18, -4, 0),
            new GuiModuleOffsets(4, 0, 4, 18, -4, 0, -4, 18)
    };

    static GuiModuleOffsets[] defaultMinorOffsets = {
            new GuiModuleOffsets(-21, 12),
            new GuiModuleOffsets(-18, 5, -18, 18),
            new GuiModuleOffsets(-12, -1, -21, 12, -12, 25),
    };

    static GuiModuleOffsets toolbeltMajorOffsets = new GuiModuleOffsets(-14, 18, 4, 0, 4, 18);
    static GuiModuleOffsets toolbeltMinorOffsets = new GuiModuleOffsets(-13, 0);

    static GuiModuleOffsets doubleMajorOffsets = new GuiModuleOffsets(-13, -1, 3, 19, -13, 19);
    static GuiModuleOffsets doubleMinorOffsets = new GuiModuleOffsets(6, 1);

    static GuiModuleOffsets bowMajorOffsets = new GuiModuleOffsets(-13, -1, 3, 19, -13, 19);
    static GuiModuleOffsets bowMinorOffsets = new GuiModuleOffsets(6, 1);

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

    public static GuiModuleOffsets getMajorOffsets(ItemModular item) {
        if (item instanceof ModularToolbeltItem) {
            return toolbeltMajorOffsets;
        } else if (item instanceof ModularDoubleHeadedItem) {
            return doubleMajorOffsets;
        } else if (item instanceof ModularBowItem) {
            return new GuiModuleOffsets(1, 21, -11, -3);
        } else if (item instanceof ModularSingleHeadedItem) {
            return new GuiModuleOffsets(1, -3, -11, 21);
        } else {
            return defaultMajorOffsets[item.getNumMajorModules() - 1];
        }
    }

    public static GuiModuleOffsets getMinorOffsets(ItemModular item) {
        if (item instanceof ModularToolbeltItem) {
            return toolbeltMinorOffsets;
        } else if (item instanceof ModularDoubleHeadedItem) {
            return doubleMinorOffsets;
        } else if (item instanceof ModularBowItem) {
            return new GuiModuleOffsets(-14, 23);
        } else if (item instanceof ModularSingleHeadedItem) {
            return new GuiModuleOffsets(-14, 0);
        } else {
            return defaultMinorOffsets[item.getNumMinorModules() - 1];
        }
    }
}
