package se.mickelus.tetra.gui.statbar.getter;

public class StatFormat {

    public static final StatFormat noDecimal = new StatFormat("%.0f");
    public static final StatFormat oneDecimal = new StatFormat("%.01f");
    public static final StatFormat twoDecimal = new StatFormat("%.02f");

    private final String format;

    public StatFormat(String format) {
        this.format = format;
    }

    public String get(double value) {
        return String.format(format, value);
    }
}
