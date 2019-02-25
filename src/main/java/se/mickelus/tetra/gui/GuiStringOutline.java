package se.mickelus.tetra.gui;

public class GuiStringOutline extends GuiString {
    public GuiStringOutline(int x, int y, String string) {
        super(x, y, string);
        drawShadow = false;
    }

    public GuiStringOutline(int x, int y, int width, String string) {
        super(x, y, width, string);
        drawShadow = false;
    }

    public GuiStringOutline(int x, int y, String string, GuiAttachment attachment) {
        super(x, y, string, attachment);
        drawShadow = false;
    }

    public GuiStringOutline(int x, int y, String string, int color) {
        super(x, y, string, color);
        drawShadow = false;
    }

    public GuiStringOutline(int x, int y, String string, int color, GuiAttachment attachment) {
        super(x, y, string, color, attachment);
        drawShadow = false;
    }

    @Override
    protected void drawString(String text, int x, int y, int color, float opacity, boolean drawShadow) {

        super.drawString(text, x - 1, y - 1, 0, opacity, false);
        super.drawString(text, x, y - 1, 0, opacity, false);
        super.drawString(text, x + 1, y - 1, 0, opacity, false);

        super.drawString(text, x - 1, y + 1, 0, opacity, false);
        super.drawString(text, x, y + 1, 0, opacity, false);
        super.drawString(text, x + 1, y + 1, 0, opacity, false);

        super.drawString(text, x + 1, y, 0, opacity, false);
        super.drawString(text, x - 1, y, 0, opacity, false);

        super.drawString(text, x, y, color, opacity, false);
    }
}
