package se.mickelus.tetra.gui.stats.bar;

import com.mojang.blaze3d.vertex.PoseStack;
import se.mickelus.mutil.gui.GuiAlignment;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class GuiBarSegmented extends GuiBar {

    private int maxSegments;
    private int segmentCount;
    private int diffCount;
    private int segmentLength;

    public GuiBarSegmented(int x, int y, int barLength, double min, double max) {
        super(x, y, barLength, min, max);

        maxSegments = (int) (max - min);
    }

    public GuiBarSegmented(int x, int y, int barLength, double min, double max, boolean invertedDiff) {
        super(x, y, barLength + 1, min, max, invertedDiff);

        maxSegments = (int) (max - min);
    }

    @Override
    protected void calculateBarLengths() {
        double minValue = Math.min(value, diffValue);

        segmentCount = (int) Math.round(minValue - min);
        diffCount = (int) Math.ceil(Math.abs(value - diffValue));

        segmentLength = width / maxSegments;
        diffColor = invertedDiff ^ value < diffValue ? increaseColorBar : decreaseColorBar;
    }

    @Override
    public void draw(PoseStack matrixStack, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        if (alignment == GuiAlignment.right) {
            for (int i = 0; i < segmentCount; i++) {
                drawSegmentReverse(matrixStack, refX, refY, i, 0xffffffff, opacity);
            }
            for (int i = segmentCount; i < segmentCount + diffCount; i++) {
                drawSegmentReverse(matrixStack, refX, refY, i, diffColor, 1);
            }
            for (int i = segmentCount + diffCount; i < maxSegments; i++) {
                drawSegmentReverse(matrixStack, refX, refY, i, 0xffffff, 0.14f * opacity);
            }
        } else {
            for (int i = 0; i < segmentCount; i++) {
                drawSegment(matrixStack, refX, refY, i, 0xffffffff, opacity);
            }
            for (int i = segmentCount; i < segmentCount + diffCount; i++) {
                drawSegment(matrixStack, refX, refY, i, diffColor, 1);
            }
            for (int i = segmentCount + diffCount; i < maxSegments; i++) {
                drawSegment(matrixStack, refX, refY, i, 0xffffff, 0.14f * opacity);
            }
        }
    }

    private void drawSegment(PoseStack matrixStack, int refX, int refY, int index, int color, float opacity) {
        drawRect(matrixStack,
            refX + x + (index * (segmentLength)),
            refY + y + 6,
            refX + x + ((index + 1) * segmentLength) - 1,
            refY + y + 6 + height,
            color, opacity);
    }

    private void drawSegmentReverse(PoseStack matrixStack, int refX, int refY, int index, int color, float opacity) {
        drawSegment(matrixStack,refX + width, refY, -index - 1, color, opacity);
    }
}
