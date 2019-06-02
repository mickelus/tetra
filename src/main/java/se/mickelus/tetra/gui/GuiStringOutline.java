package se.mickelus.tetra.gui;

public class GuiStringOutline extends GuiString {

    private static final String formatPattern = "§[0-9a-f]";
    private String cleanString;

    public GuiStringOutline(int x, int y, String string) {
        super(x, y, string);
        drawShadow = false;

        cleanString = string.replaceAll(formatPattern, "");
    }

    public GuiStringOutline(int x, int y, int width, String string) {
        super(x, y, width, string);
        drawShadow = false;

        cleanString = string.replaceAll(formatPattern, "");
    }

    public GuiStringOutline(int x, int y, String string, GuiAttachment attachment) {
        super(x, y, string, attachment);
        drawShadow = false;

        cleanString = string.replaceAll(formatPattern, "");
    }

    public GuiStringOutline(int x, int y, String string, int color) {
        super(x, y, string, color);
        drawShadow = false;

        cleanString = string.replaceAll(formatPattern, "");
    }

    public GuiStringOutline(int x, int y, String string, int color, GuiAttachment attachment) {
        super(x, y, string, color, attachment);
        drawShadow = false;

        cleanString = string.replaceAll(formatPattern, "");
    }

    @Override
    public void setString(String string) {
        super.setString(string);

        cleanString = string.replaceAll(formatPattern, "");
    }

    @Override
    protected void drawString(String text, int x, int y, int color, float opacity, boolean drawShadow) {

        super.drawString(cleanString, x - 1, y - 1, 0, opacity, false);
        super.drawString(cleanString, x, y - 1, 0, opacity, false);
        super.drawString(cleanString, x + 1, y - 1, 0, opacity, false);

        super.drawString(cleanString, x - 1, y + 1, 0, opacity, false);
        super.drawString(cleanString, x, y + 1, 0, opacity, false);
        super.drawString(cleanString, x + 1, y + 1, 0, opacity, false);

        super.drawString(cleanString, x + 1, y, 0, opacity, false);
        super.drawString(cleanString, x - 1, y, 0, opacity, false);

        super.drawString(text, x, y, color, opacity, false);
    }
}
