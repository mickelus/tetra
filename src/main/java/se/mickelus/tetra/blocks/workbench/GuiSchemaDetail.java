package se.mickelus.tetra.blocks.workbench;

import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.module.UpgradeSchema;

public class GuiSchemaDetail extends GuiElement {

    private UpgradeSchema schema;

    private GuiString title;
    private GuiTextSmall description;

    private GuiButton craftButton;

    private GuiString[] slotNames;

    public GuiSchemaDetail(int x, int y, Runnable backListener, Runnable craftListener) {
        super(x, y, 224, 64);
        addChild(new GuiButton(-4 , height + 4, 20, 18, "< back", backListener));

        title = new GuiString(4, 4, "");
        addChild(title);

        description = new GuiTextSmall(4, 16, 106, "");
        addChild(description);

        slotNames = new GuiString[3];
        for (int i = 0; i < slotNames.length; i++) {
            slotNames[i] = new GuiString(120, 6 + i * 18, "");
            addChild(slotNames[i]);
        }

        craftButton = new GuiButton(138, 50, 100, 16, "Craft", craftListener);
        addChild(craftButton);
    }

    public void setSchema(UpgradeSchema schema) {
        this.schema = schema;

        title.setString(schema.getName());
        description.setString(schema.getDescription());

        for (int i = 0; i < schema.getNumMaterialSlots(); i++) {
            slotNames[i].setString(schema.getSlotName(i));
        }
    }

    public void toggleButton(boolean enabled) {
        craftButton.setEnabled(enabled);
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        drawRect(refX + x, refY + y, refX + x + width, refY + + y + height, 0xff000000);
        super.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY);
    }
}
