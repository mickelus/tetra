package se.mickelus.tetra.gui.statbar.getter;

import com.mojang.realmsclient.gui.ChatFormatting;

public class LabelGetterBasic implements ILabelGetter {
    protected static final String increaseColorFont = ChatFormatting.GREEN.toString();
    protected static final String decreaseColorFont = ChatFormatting.RED.toString();

    protected String formatDiff;
    protected String formatDiffFlipped;
    protected String format;

    public static final ILabelGetter integerLabel = new LabelGetterBasic("%.0f", "%+.0f");
    public static final ILabelGetter decimalLabel = new LabelGetterBasic("%.02f", "%+.02f");
    public static final ILabelGetter percentageLabel = new LabelGetterBasic("%.01f%%", "%+.01f%%");

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
                return String.format(formatDiff,
                        value < diffValue ? increaseColorFont : decreaseColorFont, diffValue - value, ChatFormatting.RESET, diffValue);
            } else {
                return String.format(formatDiffFlipped,
                        diffValue, value < diffValue ? increaseColorFont : decreaseColorFont, diffValue - value);
            }
        } else {
            return String.format(format, diffValue);
        }
    }
}
