package se.mickelus.tetra.gui.statbar.getter;

import com.mojang.realmsclient.gui.ChatFormatting;

public class LabelGetterBasic implements ILabelGetter {
    protected static final String increaseColorFont = ChatFormatting.GREEN.toString();
    protected static final String decreaseColorFont = ChatFormatting.RED.toString();

    protected String formatDiff;
    protected String formatDiffFlipped;
    protected String format;

    public static final ILabelGetter integerLabel = new LabelGetterBasic("%.0f");
    public static final ILabelGetter decimalLabel = new LabelGetterBasic("%.02f");
    public static final ILabelGetter percentageLabel = new LabelGetterBasic("%.01f%%");

    public LabelGetterBasic(String format) {

        formatDiff = "%s(" + format + ") %s" + format;
        formatDiffFlipped = format + " %s(" + format + ")";
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
