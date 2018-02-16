package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
    private GuiString[] slotQuantities;
    private GuiTexture[] slotBorders;

    private GuiCapabilityIndicatorList capabilityIndicatorList;

    public GuiSchemaDetail(int x, int y, Runnable backListener, Runnable craftListener) {
        super(x, y, 224, 64);
        addChild(new GuiTexture(0, 0, width, height, 0, 68, WORKBENCH_TEXTURE));

        addChild(new GuiButton(-4 , height - 2, 40, 8, "< back", backListener));

        title = new GuiString(5, 5, "");
        addChild(title);

        description = new GuiTextSmall(5, 17, 105, "");
        addChild(description);

        slotNames = new GuiString[MAX_NUM_SLOTS];
        slotQuantities = new GuiString[MAX_NUM_SLOTS];
        slotBorders = new GuiTexture[MAX_NUM_SLOTS];
        for (int i = 0; i < MAX_NUM_SLOTS; i++) {
            slotNames[i] = new GuiString(140, 9 + i * 17, "test");
            slotNames[i].setVisible(false);
            addChild(slotNames[i]);

            slotQuantities[i] = new GuiStringSmall(139, 18 + i * 18, "/5");
            slotQuantities[i].setVisible(false);
            addChild(slotQuantities[i]);

            slotBorders[i] = new GuiTexture(121, 5 + i * 18, 16, 16, 52, 16, WORKBENCH_TEXTURE);
            slotBorders[i].setVisible(false);
            addChild(slotBorders[i]);
        }

        capabilityIndicatorList = new GuiCapabilityIndicatorList(80, 39);
        addChild(capabilityIndicatorList);

        craftButton = new GuiButton(138, 44, 30, 8, "Craft", craftListener);
        addChild(craftButton);
    }

    public void update(EntityPlayer player, UpgradeSchema schema, ItemStack itemStack, ItemStack[] materials) {
        this.schema = schema;

        title.setString(schema.getName());
        description.setString(schema.getDescription());

        for (int i = 0; i < schema.getNumMaterialSlots(); i++) {
            slotNames[i].setString(schema.getSlotName(itemStack, i));
            slotNames[i].setVisible(true);
            slotBorders[i].setVisible(true);

            if (schema.slotAcceptsMaterial(itemStack, i, materials[i])) {
                int requiredCount = schema.getRequiredQuantity(itemStack, i, materials[i]);
                if (!materials[i].isEmpty() && requiredCount > 1) {
                    slotQuantities[i].setString("/" + requiredCount);
                    slotQuantities[i].setColor(materials[i].getCount() < requiredCount ? 0xffff0000 : 0xffffffff);
                }
                slotQuantities[i].setVisible(!materials[i].isEmpty() && requiredCount > 1);
            } else {
                slotQuantities[i].setVisible(false);
            }
        }

        for (int i = schema.getNumMaterialSlots(); i < MAX_NUM_SLOTS; i++) {
            slotNames[i].setVisible(false);
            slotQuantities[i].setVisible(false);
            slotBorders[i].setVisible(false);
        }

        capabilityIndicatorList.update(player, schema, itemStack, materials);
    }

    public void toggleButton(boolean enabled) {
        craftButton.setEnabled(enabled);
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        super.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);
    }
}
