package se.mickelus.tetra.blocks.workbench.gui;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ToolType;
import se.mickelus.mgui.gui.*;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.schematic.SchematicType;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.List;
import java.util.Map;

public class GuiSchematicDetail extends GuiElement {

    private static final int MAX_NUM_SLOTS = 2;

    private UpgradeSchematic schematic;

    private GuiElement glyph;
    private GuiString title;
    private GuiTextSmall description;
    private List<String> descriptionTooltip;

    private CraftButtonGui craftButton;

    private GuiString[] slotNames;
    private GuiString[] slotQuantities;
    private GuiItemRolling[] slotPlaceholders;
    private GuiTexture[] slotBorders;

    private GuiMagicUsage magicCapacity;

    private ToolRequirementListGui toolRequirementList;

    private GuiExperience experienceIndicator;

    public GuiSchematicDetail(int x, int y, Runnable backListener, Runnable craftListener) {
        super(x, y, 224, 67);
        addChild(new GuiButton(-4 , height - 2, 40, 8, "< " + I18n.format("tetra.workbench.schematic_detail.back"), backListener));

        glyph = new GuiElement(3, 3, 16, 16);
        addChild(glyph);

        title = new GuiString(19, 6, 100, "");
        addChild(title);

        description = new GuiTextSmall(5, 17, 105, "");
        addChild(description);

        slotNames = new GuiString[MAX_NUM_SLOTS];
        slotQuantities = new GuiString[MAX_NUM_SLOTS];
        slotPlaceholders = new GuiItemRolling[MAX_NUM_SLOTS];
        slotBorders = new GuiTexture[MAX_NUM_SLOTS];
        for (int i = 0; i < MAX_NUM_SLOTS; i++) {
            slotNames[i] = new GuiString(140, 9 + i * 17, "");
            slotNames[i].setVisible(false);
            addChild(slotNames[i]);

            slotQuantities[i] = new GuiStringSmall(139, 18 + i * 18, "");
            slotQuantities[i].setVisible(false);
            addChild(slotQuantities[i]);

            slotPlaceholders[i] = new GuiItemRolling(121, 5 + i * 18);
            slotPlaceholders[i].setVisible(false);
            slotPlaceholders[i].setCountVisibility(GuiItem.CountMode.never);
            addChild(slotPlaceholders[i]);

            slotBorders[i] = new GuiTexture(121, 5 + i * 18, 16, 16, 52, 16, GuiTextures.workbench);
            slotBorders[i].setOpacity(0.8f);
            slotBorders[i].setVisible(false);
            addChild(slotBorders[i]);
        }

        magicCapacity = new GuiMagicUsage(121, 28,80);
        addChild(magicCapacity);

        toolRequirementList = new ToolRequirementListGui(80, 39);
        addChild(toolRequirementList);

        experienceIndicator = new GuiExperience(192, 42, "tetra.workbench.schematic_detail.experience");
        addChild(experienceIndicator);

        craftButton = new CraftButtonGui(140, 40, craftListener);
        addChild(craftButton);
    }

    public void update(UpgradeSchematic schematic, ItemStack itemStack, String slot, ItemStack[] materials, Map<ToolType, Integer> availableTools,
            int playerLevel) {
        this.schematic = schematic;

        title.setString(schematic.getName());
        title.setColor(schematic.getRarity().tint);

        String descriptionString = schematic.getDescription(itemStack);
        description.setString(TextFormatting.GRAY + descriptionString
                .replace(TextFormatting.RESET.toString(), TextFormatting.RESET.toString() + TextFormatting.GRAY.toString()));
        descriptionTooltip = ImmutableList.of(descriptionString);

        glyph.clearChildren();
        GlyphData glyphData = schematic.getGlyph();
        GuiTexture border = null;
        GuiTexture glyphTexture;
        if (schematic.getType() == SchematicType.major) {
            border = new GuiTexture(0, 2, 16, 9, 52, 3, GuiTextures.workbench);
            glyphTexture = new GuiTexture(-1, -1, 16, 16, glyphData.textureX, glyphData.textureY, glyphData.textureLocation);
        } else if (schematic.getType() == SchematicType.minor) {
            border = new GuiTexture(2, 1, 11, 11, 68, 0, GuiTextures.workbench);
            glyphTexture = new GuiTexture(4, 3, 8, 8, glyphData.textureX, glyphData.textureY, glyphData.textureLocation);
        } else if (schematic.getType() == SchematicType.improvement) {
            border = new GuiTexture(0, 2, 16, 9, 52, 3, GuiTextures.workbench);
            glyphTexture = new GuiTexture(-1, -1, 16, 16, glyphData.textureX, glyphData.textureY, glyphData.textureLocation);
        } else {
            glyphTexture = new GuiTexture(-1, -1, 16, 16, glyphData.textureX, glyphData.textureY, glyphData.textureLocation);
        }

        if (border != null) {
            border.setOpacity(0.3f);
            border.setColor(schematic.getRarity().tint);
            glyph.addChild(border);
        }
        glyphTexture.setColor(schematic.getRarity().tint);
        glyph.addChild(glyphTexture);

        if (schematic.getType() == SchematicType.improvement) {
            glyph.addChild(new GuiTexture(7, 7, 7, 7, 68, 16, GuiTextures.workbench));
        }

        for (int i = 0; i < schematic.getNumMaterialSlots(); i++) {
            slotNames[i].setString(schematic.getSlotName(itemStack, i));
            slotNames[i].setVisible(true);

            slotPlaceholders[i].setVisible(i < materials.length && materials[i].isEmpty());
            slotPlaceholders[i].setItems(schematic.getSlotPlaceholders(itemStack, i));

            slotBorders[i].setVisible(true);

            if (schematic.acceptsMaterial(itemStack, slot, i, materials[i])) {
                int requiredCount = schematic.getRequiredQuantity(itemStack, i, materials[i]);
                if (!materials[i].isEmpty() && requiredCount > 1) {
                    slotQuantities[i].setString("/" + requiredCount);
                    slotQuantities[i].setColor(materials[i].getCount() < requiredCount ? GuiColors.negative : GuiColors.normal);
                }
                slotQuantities[i].setVisible(!materials[i].isEmpty() && requiredCount > 1);
            } else {
                slotQuantities[i].setVisible(false);
            }
        }

        for (int i = schematic.getNumMaterialSlots(); i < MAX_NUM_SLOTS; i++) {
            slotNames[i].setVisible(false);
            slotQuantities[i].setVisible(false);
            slotPlaceholders[i].setVisible(false);
            slotBorders[i].setVisible(false);
        }

        toolRequirementList.update(schematic, itemStack, slot, materials, availableTools);

        int xpCost = schematic.getExperienceCost(itemStack, materials, slot);
        experienceIndicator.setVisible(xpCost > 0);
        if (xpCost > 0) {
            experienceIndicator.update(xpCost, xpCost <= playerLevel);
        }
    }

    public void updateMagicCapacity(UpgradeSchematic schematic, String slot, ItemStack itemStack, ItemStack previewStack) {
        if (slot != null && (schematic != null && SchematicType.major.equals(schematic.getType()) && magicCapacity.providesCapacity(itemStack, previewStack, slot)
                || magicCapacity.hasChanged(itemStack, previewStack, slot))) {
            magicCapacity.update(itemStack, previewStack, slot);
            magicCapacity.setVisible(true);
        } else {
            magicCapacity.setVisible(false);
        }
    }

    public void updateAvailableTools(Map<ToolType, Integer> availableTools) {
        toolRequirementList.updateAvailableTools(availableTools);
    }

    public void updateButton(UpgradeSchematic schematic, PlayerEntity player, ItemStack itemStack, ItemStack previewStack, ItemStack[] materials, String slot,
            Map<ToolType, Integer> availableTools) {
        craftButton.update(schematic, player, itemStack, previewStack, materials, slot, availableTools);
    }

    @Override
    public List<String> getTooltipLines() {
        if (description.hasFocus()) {
            return descriptionTooltip;
        }

        return super.getTooltipLines();
    }
}
