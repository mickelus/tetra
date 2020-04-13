package se.mickelus.tetra.items.journal.gui.craft;

import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;

public class GuiJournalSeparators extends GuiElement {
    public GuiJournalSeparators(int x, int y, int width, int height) {
        super(x, y, width, height);

        int crossOffset = 40;

        int diagonalOffset = 16;
        int diagonalSize = 9;

        int delay = 800;

        // center
        addChild(new GuiJournalDiagonal(-diagonalOffset, -diagonalOffset + 1, diagonalSize, GuiAttachment.bottomRight, delay * 1 + (int) (Math.random() * delay)));
        addChild(new GuiJournalDiagonal(-diagonalOffset, diagonalOffset, diagonalSize, GuiAttachment.topRight, delay * 3 + (int) (Math.random() * delay)));
        addChild(new GuiJournalCross(crossOffset - 1, -crossOffset, delay * 4 + (int) (Math.random() * delay)));
        addChild(new GuiJournalCross(crossOffset - 1, crossOffset, delay * 6 + (int) (Math.random() * delay)));

        addChild(new GuiJournalDiagonal(diagonalOffset - 1, -diagonalOffset + 1, diagonalSize,GuiAttachment.bottomLeft, delay * 2 + (int) (Math.random() * delay)));
        addChild(new GuiJournalDiagonal(diagonalOffset - 1, diagonalOffset, diagonalSize, GuiAttachment.topLeft, delay * 4 + (int) (Math.random() * delay)));
        addChild(new GuiJournalCross(-crossOffset - 1, -crossOffset, delay * 3 + (int) (Math.random() * delay)));
        addChild(new GuiJournalCross(-crossOffset - 1, crossOffset, delay * 5 + (int) (Math.random() * delay)));

        // right
        addChild(new GuiJournalDiagonal(-diagonalOffset + 80, -diagonalOffset + 1, diagonalSize, GuiAttachment.bottomRight, delay * 5 + (int) (Math.random() * delay)));
        addChild(new GuiJournalDiagonal(-diagonalOffset + 80, diagonalOffset, diagonalSize,GuiAttachment.topRight, delay * 7 + (int) (Math.random() * delay)));
        addChild(new GuiJournalCross(79, 0, delay * 8 + (int) (Math.random() * delay)));
        addChild(new GuiJournalDiagonal(diagonalOffset - 1 + 80, -diagonalOffset + 1, diagonalSize,GuiAttachment.bottomLeft, delay * 9 + (int) (Math.random() * delay)));
        addChild(new GuiJournalDiagonal(diagonalOffset - 1 + 80, diagonalOffset, diagonalSize, GuiAttachment.topLeft, delay * 10 + (int) (Math.random() * delay)));

        addChild(new GuiJournalCross(119, -crossOffset, delay * 10 + (int) (Math.random() * delay)));
        addChild(new GuiJournalCross(119, crossOffset, delay * 11 + (int) (Math.random() * delay)));
        addChild(new GuiJournalDiagonal(-diagonalOffset + 160, -diagonalOffset + 1, diagonalSize, GuiAttachment.bottomRight, delay * 12 + (int) (Math.random() * delay)));
        addChild(new GuiJournalDiagonal(-diagonalOffset + 160, diagonalOffset, diagonalSize,GuiAttachment.topRight, delay * 13 + (int) (Math.random() * delay)));

        addChild(new GuiJournalCross(159, 0, delay * 14 + (int) (Math.random() * delay)));
        addChild(new GuiJournalDiagonal(diagonalOffset - 1 + 160, -diagonalOffset + 1, diagonalSize,GuiAttachment.bottomLeft, delay * 15 + (int) (Math.random() * delay)));
        addChild(new GuiJournalDiagonal(diagonalOffset - 1 + 160, diagonalOffset, diagonalSize, GuiAttachment.topLeft, delay * 16 + (int) (Math.random() * delay)));
        addChild(new GuiJournalCross(199, -crossOffset, delay * 18 + (int) (Math.random() * delay)));
        addChild(new GuiJournalCross(199, crossOffset, delay * 17 + (int) (Math.random() * delay)));

        // left
        addChild(new GuiJournalDiagonal(diagonalOffset - 1 - 80, -diagonalOffset + 1, diagonalSize,GuiAttachment.bottomLeft, delay * 4 + (int) (Math.random() * delay)));
        addChild(new GuiJournalDiagonal(diagonalOffset - 1 - 80, diagonalOffset, diagonalSize, GuiAttachment.topLeft, delay * 6 + (int) (Math.random() * delay)));
        addChild(new GuiJournalCross(-81, 0, delay * 7 + (int) (Math.random() * delay)));
        addChild(new GuiJournalDiagonal(-diagonalOffset - 80, -diagonalOffset + 1, diagonalSize, GuiAttachment.bottomRight, delay * 9 + (int) (Math.random() * delay)));
        addChild(new GuiJournalDiagonal(-diagonalOffset - 80, diagonalOffset, diagonalSize,GuiAttachment.topRight, delay * 8 + (int) (Math.random() * delay)));

        addChild(new GuiJournalCross(-121, -crossOffset, delay * 10 + (int) (Math.random() * delay)));
        addChild(new GuiJournalCross(-121, crossOffset, delay * 11 + (int) (Math.random() * delay)));
        addChild(new GuiJournalDiagonal(diagonalOffset - 1 - 160, -diagonalOffset + 1, diagonalSize,GuiAttachment.bottomLeft, delay * 13 + (int) (Math.random() * delay)));
        addChild(new GuiJournalDiagonal(diagonalOffset - 1 - 160, diagonalOffset, diagonalSize, GuiAttachment.topLeft, delay * 12 + (int) (Math.random() * delay)));

        addChild(new GuiJournalCross(-161, 0, delay * 14 + (int) (Math.random() * delay)));
        addChild(new GuiJournalDiagonal(-diagonalOffset - 160, -diagonalOffset + 1, diagonalSize, GuiAttachment.bottomRight, delay * 15 + (int) (Math.random() * delay)));
        addChild(new GuiJournalDiagonal(-diagonalOffset - 160, diagonalOffset, diagonalSize,GuiAttachment.topRight, delay * 16 + (int) (Math.random() * delay)));
        addChild(new GuiJournalCross(-201, -crossOffset, delay * 18 + (int) (Math.random() * delay)));
        addChild(new GuiJournalCross(-201, crossOffset, delay * 17 + (int) (Math.random() * delay)));
    }

    public void animateOpen() {
        setVisible(true);
        getChildren(GuiJournalCross.class).forEach(GuiJournalCross::animateOpen);
        getChildren(GuiJournalDiagonal.class).forEach(GuiJournalDiagonal::animateOpen);
    }

    public void animateReopen() {
        setVisible(true);
        getChildren(GuiJournalCross.class).forEach(GuiJournalCross::animateReopen);
        getChildren(GuiJournalDiagonal.class).forEach(GuiJournalDiagonal::animateReopen);
    }
}