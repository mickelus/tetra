package se.mickelus.tetra.blocks.workbench.gui;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import se.mickelus.mutil.gui.*;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiItemRolling;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.gui.ZOffsetGui;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.schematic.HoloMaterialApplicable;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.schematic.HoloMaterialTranslationGui;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.List;

public class SchemaSlotGui extends GuiElement {
    private final GuiString label;
    private final GuiString quantity;
    private final GuiItemRolling placeholder;
    private final GuiElement placeholderBorder;
    private final GuiTexture border;

    private final HoloMaterialTranslationGui materialTranslation;
    private final HoloMaterialApplicable applicableMaterials;

    private final int index;

    private final int fullWidth;
    private static final int compactWidth = 26;

    private List<Component> labelTooltip = null;

    public SchemaSlotGui(int x, int y, int width, int index) {
        super(x, y, width, 18);

        fullWidth = width;

        label = new GuiString(28, 5, width - 30, "");
        addChild(label);

        quantity = new GuiStringSmall(27, 14, "");
        addChild(quantity);

        placeholder = new GuiItemRolling(10, 1);
        placeholder.setCountVisibility(GuiItem.CountMode.never);
        addChild(placeholder);

        placeholderBorder = new ZOffsetGui(0, 0, 160);
        placeholderBorder.addChild(new GuiTexture(10, 1, 16, 16, 52, 16, GuiTextures.workbench).setOpacity(0.8f));
        addChild(placeholderBorder);

        border = new GuiTexture(10, 1, 16, 16, 52, 16, GuiTextures.workbench);
        border.setOpacity(0.8f);
        addChild(border);

        materialTranslation = new HoloMaterialTranslationGui(1, 1);
        addChild(materialTranslation);
        applicableMaterials = new HoloMaterialApplicable(1, 9);
        addChild(applicableMaterials);

        this.index = index;

//        addChild(new GuiRect(0, 0, width, height, GuiColors.hover));
    }

    public void update(UpgradeSchematic schematic, Player player, Level level, BlockPos pos, WorkbenchTile blockEntity, ItemStack targetStack,
            String slot, ItemStack[] materials) {
        int slotCount = schematic.getNumMaterialSlots();
        if (slotCount > index) {
            boolean slotHoldsMaterial = index < materials.length && materials[index].isEmpty();

            setWidth(slotCount > 1 ? compactWidth : fullWidth);
            label.setVisible(slotCount == 1);

            // todo: make materials explanations
            materialTranslation.setVisible(index == 0);
            applicableMaterials.setVisible(index == 0);
            if (index == 0) {
                materialTranslation.update(schematic);
                applicableMaterials.update(level, pos, blockEntity, targetStack, slot, schematic, player);
            }

            border.setVisible(!slotHoldsMaterial);
            placeholderBorder.setVisible(slotHoldsMaterial);
            placeholder.setVisible(slotHoldsMaterial);
            placeholder.setItems(schematic.getSlotPlaceholders(targetStack, index));


            String labelString = schematic.getSlotName(targetStack, index);
            label.setString(labelString);
            labelTooltip = slotHoldsMaterial
                    ? ImmutableList.of(Component.literal(labelString))
                    : null;

            if (index < materials.length && schematic.acceptsMaterial(targetStack, slot, index, materials[index])) {
                int requiredCount = schematic.getRequiredQuantity(targetStack, index, materials[index]);
                if (!materials[index].isEmpty() && requiredCount > 1) {
                    quantity.setString("/" + requiredCount);
                    quantity.setColor(materials[index].getCount() < requiredCount ? GuiColors.negative : GuiColors.normal);
                }
                quantity.setVisible(!materials[index].isEmpty() && requiredCount > 1);
            } else {
                quantity.setVisible(false);
            }

            setVisible(true);
        } else {
            setVisible(false);
        }
    }

    @Override
    public List<Component> getTooltipLines() {
        List<Component> tooltipLines = super.getTooltipLines();
        if (tooltipLines == null && hasFocus()) {
            return labelTooltip;
        }
        return tooltipLines;
    }
}
