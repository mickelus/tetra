package se.mickelus.tetra.gui.stats.getter;

public interface ILabelGetter {
    String getLabel(double value, double diffValue, boolean flipped);

    String getLabelMerged(double value, double diffValue);
}
