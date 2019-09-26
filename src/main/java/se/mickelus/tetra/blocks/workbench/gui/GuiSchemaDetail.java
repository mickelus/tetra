package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.gui.impl.GuiMagicUsage;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.schema.SchemaType;
import se.mickelus.tetra.module.schema.UpgradeSchema;

public class GuiSchemaDetail extends GuiElement {

    private static final String WORKBENCH_TEXTURE = "textures/gui/workbench.png";

    private static final int MAX_NUM_SLOTS = 2;

    private UpgradeSchema schema;

    private GuiElement glyph;
    private GuiString title;
    private GuiTextSmall description;

    private GuiButton craftButton;

    private GuiString[] slotNames;
    private GuiString[] slotQuantities;
    private GuiTexture[] slotBorders;

    private GuiMagicUsage magicCapacity;

    private GuiCapabilityRequirementList capabilityIndicatorList;

    private GuiExperience experienceIndicator;

    public GuiSchemaDetail(int x, int y, Runnable backListener, Runnable craftListener) {
        super(x, y, 224, 67);
        addChild(new GuiButton(-4 , height - 2, 40, 8, "< " + I18n.format("workbench.schema_detail.back"), backListener));

        glyph = new GuiElement(3, 3, 16, 16);
        addChild(glyph);

        title = new GuiString(19, 6, 100, "");
        addChild(title);

        description = new GuiTextSmall(5, 17, 105, "");
        addChild(description);

        slotNames = new GuiString[MAX_NUM_SLOTS];
        slotQuantities = new GuiString[MAX_NUM_SLOTS];
        slotBorders = new GuiTexture[MAX_NUM_SLOTS];
        for (int i = 0; i < MAX_NUM_SLOTS; i++) {
            slotNames[i] = new GuiString(140, 9 + i * 17, "");
            slotNames[i].setVisible(false);
            addChild(slotNames[i]);

            slotQuantities[i] = new GuiStringSmall(139, 18 + i * 18, "");
            slotQuantities[i].setVisible(false);
            addChild(slotQuantities[i]);

            slotBorders[i] = new GuiTexture(121, 5 + i * 18, 16, 16, 52, 16, WORKBENCH_TEXTURE);
            slotBorders[i].setVisible(false);
            addChild(slotBorders[i]);
        }

        magicCapacity = new GuiMagicUsage(121, 28,80);
        addChild(magicCapacity);

        capabilityIndicatorList = new GuiCapabilityRequirementList(80, 39);
        addChild(capabilityIndicatorList);

        experienceIndicator = new GuiExperience(170, 42, "workbench.schema_detail.experience");
        addChild(experienceIndicator);

        craftButton = new GuiButton(138, 44, 30, 8, I18n.format("workbench.schema_detail.craft"), craftListener);
        addChild(craftButton);
    }

    public void update(UpgradeSchema schema, ItemStack itemStack, ItemStack[] materials, int[] availableCapabilities, int playerLevel) {
        this.schema = schema;

        title.setString(schema.getName());
        title.setColor(schema.getRarity().tint);
        description.setString(schema.getDescription(itemStack));

        glyph.clearChildren();
        GlyphData glyphData = schema.getGlyph();
        GuiTexture border = null;
        GuiTexture glyphTexture;
        if (schema.getType() == SchemaType.major) {
            border = new GuiTexture(0, 2, 16, 9, 52, 3, WORKBENCH_TEXTURE);
            glyphTexture = new GuiTexture(-1, -1, 16, 16, glyphData.textureX, glyphData.textureY, glyphData.textureLocation);
        } else if (schema.getType() == SchemaType.minor) {
            border = new GuiTexture(2, 1, 11, 11, 68, 0, WORKBENCH_TEXTURE);
            glyphTexture = new GuiTexture(4, 3, 8, 8, glyphData.textureX, glyphData.textureY, glyphData.textureLocation);
        } else if (schema.getType() == SchemaType.improvement) {
            border = new GuiTexture(0, 2, 16, 9, 52, 3, WORKBENCH_TEXTURE);
            glyphTexture = new GuiTexture(-1, -1, 16, 16, glyphData.textureX, glyphData.textureY, glyphData.textureLocation);
        } else {
            glyphTexture = new GuiTexture(-1, -1, 16, 16, glyphData.textureX, glyphData.textureY, glyphData.textureLocation);
        }

        if (border != null) {
            border.setOpacity(0.3f);
            border.setColor(schema.getRarity().tint);
            glyph.addChild(border);
        }
        glyphTexture.setColor(schema.getRarity().tint);
        glyph.addChild(glyphTexture);

        if (schema.getType() == SchemaType.improvement) {
            glyph.addChild(new GuiTexture(7, 7, 7, 7, 68, 16, WORKBENCH_TEXTURE));
        }


        for (int i = 0; i < schema.getNumMaterialSlots(); i++) {
            slotNames[i].setString(schema.getSlotName(itemStack, i));
            slotNames[i].setVisible(true);
            slotBorders[i].setVisible(true);

            if (schema.acceptsMaterial(itemStack, i, materials[i])) {
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

        capabilityIndicatorList.update(schema, itemStack, materials, availableCapabilities);

        int xpCost = schema.getExperienceCost(itemStack, materials);
        experienceIndicator.setVisible(xpCost > 0);
        if (xpCost > 0) {
            experienceIndicator.update(xpCost, xpCost <= playerLevel);
        }
    }

    public void updateMagicCapacity(UpgradeSchema schema, String slot, ItemStack itemStack, ItemStack previewStack) {
        if (slot != null && (schema != null && SchemaType.major.equals(schema.getType()) && magicCapacity.providesCapacity(itemStack, previewStack, slot)
                || magicCapacity.hasChanged(itemStack, previewStack, slot))) {
            magicCapacity.update(itemStack, previewStack, slot);
            magicCapacity.setVisible(true);
        } else {
            magicCapacity.setVisible(false);
        }
    }

    public void updateAvailableCapabilities(int[] availableCapabilities) {
        capabilityIndicatorList.updateAvailableCapabilities(availableCapabilities);
    }

    public void toggleButton(boolean enabled) {
        craftButton.setEnabled(enabled);
    }
}
