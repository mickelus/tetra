package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiRect;
import se.mickelus.mutil.gui.GuiString;
import se.mickelus.tetra.gui.GuiItemRolling;

import java.util.Collections;
import java.util.List;

public class RepairInfoGui extends GuiElement {
    private final GuiString repairTitle;
    private final GuiString noRepairLabel;
    private final GuiRect noRepairBackdrop;
    private final GuiItemRolling repairMaterial;
    private boolean canRepair;

    private final List<Component> tooltip;
    private final List<Component> noRepairtooltip;

    public RepairInfoGui(int x, int y) {
        super(x, y, 49, 16);

//        addChild(new GuiRect(-8, 0, 16, 12, GuiColors.hover).setAttachment(GuiAttachment.middleCenter));

        repairTitle = new GuiString(-8, 0, I18n.get("item.tetra.modular.repair_material.label"));
        repairTitle.setAttachment(GuiAttachment.middleCenter);
        addChild(repairTitle);


        noRepairBackdrop = new GuiRect(0, 0, 49, 16, 0);
        noRepairBackdrop.setVisible(false);
        addChild(noRepairBackdrop);
        noRepairLabel = new GuiString(1, 0, ChatFormatting.DARK_GRAY + I18n.get("item.tetra.modular.repair_material.empty"));
        noRepairLabel.setAttachment(GuiAttachment.middleCenter);
        noRepairLabel.setVisible(false);
        addChild(noRepairLabel);

        repairMaterial = new GuiItemRolling(0, 0);
        repairMaterial.setAttachment(GuiAttachment.topRight);
        addChild(repairMaterial);

        tooltip = Collections.singletonList(Component.translatable("item.tetra.modular.repair_material.tooltip"));
        noRepairtooltip = Collections.singletonList(Component.translatable("item.tetra.modular.repair_material.empty_tooltip"));
    }

    public void update(ItemStack[] repairItemStacks) {
        repairMaterial.setItems(repairItemStacks);

        canRepair = repairItemStacks.length > 0;
        repairTitle.setVisible(canRepair);
        repairMaterial.setVisible(canRepair);
        noRepairLabel.setVisible(!canRepair);
        noRepairBackdrop.setVisible(!canRepair);

    }

    @Override
    public List<Component> getTooltipLines() {
        List<Component> childTooltip = super.getTooltipLines();

        if (hasFocus() && childTooltip == null) {
            return canRepair
                    ? tooltip
                    : noRepairtooltip;
        }
        return childTooltip;
    }
}
