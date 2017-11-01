package se.mickelus.tetra.blocks.workbench.gui;

import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.module.UpgradeSchema;

public class GuiSchemaDetail extends GuiElement {

    private static final String WORKBENCH_TEXTURE = "textures/gui/workbench.png";

    private static final int MAX_NUM_SLOTS = 2;

    private UpgradeSchema schema;

    private GuiString title;
    private GuiTextSmall description;

    private GuiButton craftButton;

    private GuiString[] slotNames;
    private GuiTexture[] slotBorders;

    public GuiSchemaDetail(int x, int y, Runnable backListener, Runnable craftListener) {
        super(x, y, 224, 64);
        addChild(new GuiButton(-4 , height + 4, 40, 8, "< back", backListener));

        title = new GuiString(4, 4, "");
        addChild(title);

        description = new GuiTextSmall(4, 16, 106, "");
        addChild(description);

        slotNames = new GuiString[MAX_NUM_SLOTS];
        slotBorders = new GuiTexture[MAX_NUM_SLOTS];
        for (int i = 0; i < MAX_NUM_SLOTS; i++) {
            slotNames[i] = new GuiString(140, 8 + i * 17, "");
            slotNames[i].setVisible(false);
            addChild(slotNames[i]);

            slotBorders[i] = new GuiTexture(121, 4 + i * 17, 16, 16, 52, 16, WORKBENCH_TEXTURE);
            slotBorders[i].setVisible(false);
            addChild(slotBorders[i]);
        }

        craftButton = new GuiButton(138, 50, 30, 8, "Craft", craftListener);
        addChild(craftButton);
    }

    public void setSchema(UpgradeSchema schema) {
        this.schema = schema;

        title.setString(schema.getName());
        description.setString(schema.getDescription());

        for (int i = 0; i < schema.getNumMaterialSlots(); i++) {
            slotNames[i].setString(schema.getSlotName(i));
            slotNames[i].setVisible(true);
            slotBorders[i].setVisible(true);
        }

        for (int i = schema.getNumMaterialSlots(); i < MAX_NUM_SLOTS; i++) {
            slotNames[i].setVisible(false);
            slotBorders[i].setVisible(false);
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
