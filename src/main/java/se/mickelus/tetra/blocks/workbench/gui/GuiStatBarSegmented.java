package se.mickelus.tetra.blocks.workbench.gui;

import se.mickelus.tetra.gui.GuiAlignment;

public class GuiStatBarSegmented extends GuiStatBar {
    int maxSegments, segmentCount, diffCount, segmentLength;
    public GuiStatBarSegmented(int x, int y, String label, double min, double max) {
        super(x, y, label, min, max);
    }

    @Override
    protected void calculateBarLengths() {
        double minValue = Math.min(value, diffValue);


        maxSegments = (int) Math.ceil(max - min);
        segmentCount = (int) Math.round(minValue - min);
        diffCount = (int) Math.ceil(Math.abs(value - diffValue));

        segmentLength = barMaxLength / maxSegments;
        diffColor = value < diffValue ? increaseColorBar : decreaseColorBar;
    }

    @Override
    protected void drawBar(int refX, int refY) {
        if (alignment == GuiAlignment.right) {
            for (int i = 0; i < segmentCount; i++) {
                drawSegmentReverse(refX, refY, i, 0xffffffff);
            }
            for (int i = segmentCount; i < segmentCount + diffCount; i++) {
                drawSegmentReverse(refX, refY, i, diffColor);
            }
            for (int i = segmentCount + diffCount; i < maxSegments; i++) {
                drawSegmentReverse(refX, refY, i, 0x22ffffff);
            }
        } else {
            for (int i = 0; i < segmentCount; i++) {
                drawSegment(refX, refY, i, 0xffffffff);
            }
            for (int i = segmentCount; i < segmentCount + diffCount; i++) {
                drawSegment(refX, refY, i, diffColor);
            }
            for (int i = segmentCount + diffCount; i < maxSegments; i++) {
                drawSegment(refX, refY, i, 0x22ffffff);
            }
        }
    }

    private void drawSegment(int refX, int refY, int index, int color) {
        drawRect(
            refX + x + (index * (segmentLength)),
            refY + y + 6,
            refX + x + ((index + 1) * segmentLength) - 1,
            refY + y + 6 + barHeight,
            color);
    }

    private void drawSegmentReverse(int refX, int refY, int index, int color) {
        drawSegment(refX + barMaxLength + 1, refY, -index - 1, color);
    }
}
