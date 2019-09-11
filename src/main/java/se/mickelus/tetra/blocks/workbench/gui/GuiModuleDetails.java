package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.gui.animation.Applier;
import se.mickelus.tetra.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.impl.statbar.GuiBar;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.schema.RepairDefinition;
import se.mickelus.tetra.module.schema.SchemaType;
import se.mickelus.tetra.module.schema.UpgradeSchema;

public class GuiModuleDetails extends GuiElement {

    private GuiElement glyph;
    private GuiString title;
    private GuiTextSmall description;
    private GuiString emptyLabel;


    private GuiElement repairGroup;
    private GuiStringSmall repairTitle;
    private GuiItem repairMaterial;
    private GuiStringSmall noRepairLabel;

    public GuiModuleDetails(int x, int y) {
        super(x, y, 224, 67);

        glyph = new GuiElement(3, 3, 16, 16);
        addChild(glyph);

        title = new GuiString(20, 7, 195, "");
        addChild(title);

        description = new GuiTextSmall(5, 19, 105, "");
        addChild(description);

        emptyLabel = new GuiString(0, -3, TextFormatting.DARK_GRAY + I18n.format("workbench.module_detail.empty"));
        emptyLabel.setAttachment(GuiAttachment.middleCenter);
        addChild(emptyLabel);

        repairGroup = new GuiElement(130, 19, 80, 16);
        addChild(repairGroup);

        repairTitle = new GuiStringSmall(0, 7, I18n.format("item.modular.repair_material.label"));
        repairGroup.addChild(repairTitle);

        noRepairLabel = new GuiStringSmall(0, 7, TextFormatting.GRAY + I18n.format("item.modular.repair_material.empty"));
        noRepairLabel.setAttachment(GuiAttachment.topCenter);
        noRepairLabel.setVisible(false);
        repairGroup.addChild(noRepairLabel);

        repairMaterial = new GuiItem(-2, 0);
        repairMaterial.setAttachment(GuiAttachment.topRight);
        repairGroup.addChild(repairMaterial);

    }

    public void update(ItemModule module, ItemStack itemStack) {
        glyph.clearChildren();
        if (module != null) {
            title.setString(module.getName(itemStack));
            description.setString(module.getDescription(itemStack));

            GlyphData glyphData = module.getData(itemStack).glyph;
            if (module instanceof ItemModuleMajor) {
                glyph.addChild(new GuiTexture(0, 0, 15, 15, 52, 0, "textures/gui/workbench.png"));
                glyph.addChild(new GuiModuleGlyph(-1, 0, 16, 16, glyphData).setShift(false));
            } else {
                glyph.addChild(new GuiTexture(3, 2, 11, 11, 68, 0, "textures/gui/workbench.png"));
                glyph.addChild(new GuiModuleGlyph(5, 4, 8, 8, glyphData).setShift(false));
            }


            RepairDefinition repairDefinition = ItemUpgradeRegistry.instance.getRepairDefinition(module.getData(itemStack).key);
            boolean canRepair = repairDefinition != null && repairDefinition.material.getApplicableItemstacks().length > 0;
            if (canRepair) {
                repairMaterial.setItem(repairDefinition.material.getApplicableItemstacks()[0]);
            }

            repairTitle.setVisible(canRepair);
            repairMaterial.setVisible(canRepair);
            noRepairLabel.setVisible(!canRepair);
        }

        title.setVisible(module != null);
        description.setVisible(module != null);
        emptyLabel.setVisible(module == null);
        repairGroup.setVisible(module != null);
    }
}
