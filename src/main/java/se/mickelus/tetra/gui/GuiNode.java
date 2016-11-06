package se.mickelus.tetra.gui;

import net.minecraft.client.gui.Gui;
import java.util.ArrayList;

public class GuiNode extends Gui {

    protected ArrayList<GuiNode> elements;

    public GuiNode() {
        elements = new ArrayList<>();
    }

    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        drawChildren(refX, refY, screenWidth, screenHeight, mouseX, mouseY);
    }

    protected void drawChildren(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        elements.stream().forEach((element -> element.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY)));
    }

    public void addChild(GuiNode child) {
        this.elements.add(child);
    }

    public void clearChildren() {
        this.elements.clear();
    }

    public int getNumChildren() {
        return elements.size();
    }

    public GuiNode getChild(int index) {
        if (index >= 0 && index < elements.size()) {
            return elements.get(index);
        }
        return null;
    }
}
