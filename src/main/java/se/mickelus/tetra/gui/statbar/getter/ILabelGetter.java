package se.mickelus.tetra.gui.statbar.getter;

public interface ILabelGetter {
    public String getLabel(double value, double diffValue, boolean flipped);
    public String getLabelMerged(double value, double diffValue);
}
