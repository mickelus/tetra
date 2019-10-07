package se.mickelus.tetra.gui.impl.statbar.getter;

import net.minecraft.util.text.TextFormatting;

public class LabelGetterBasic implements ILabelGetter {
    protected static final String increaseColorFont = TextFormatting.GREEN.toString();
    protected static final String decreaseColorFont = TextFormatting.RED.toString();

    protected String formatDiff;
    protected String formatDiffFlipped;
    protected String format;

    public static final ILabelGetter integerLabel = new LabelGetterBasic("%.0f", "%+.0f");
    public static final ILabelGetter decimalLabel = new LabelGetterBasic("%.02f", "%+.02f");
    public static final ILabelGetter percentageLabel = new LabelGetterBasic("%.0f%%", "%+.0f%%");
    public static final ILabelGetter percentageLabelDecimal = new LabelGetterBasic("%.01f%%", "%+.01f%%");

    public LabelGetterBasic(String format) {
        this(format, format);
    }

    public LabelGetterBasic(String format, String formatDiff) {

        this.formatDiff = "%s(" + formatDiff + ") %s" + format;
        formatDiffFlipped = format + " %s(" + formatDiff + ")";
        this.format = format;
    }


    @Override
    public String getLabel(double value, double diffValue, boolean flipped) {
        if (value != diffValue) {
            if (flipped) {
                return String.format(formatDiffFlipped,
                        diffValue, value < diffValue ? increaseColorFont : decreaseColorFont, diffValue - value);
            } else {
                return String.format(formatDiff,
                        value < diffValue ? increaseColorFont : decreaseColorFont, diffValue - value, TextFormatting.RESET, diffValue);
            }
        } else {
            return String.format(format, diffValue);
        }
    }
}
