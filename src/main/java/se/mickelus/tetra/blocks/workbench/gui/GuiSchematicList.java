package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import se.mickelus.mgui.gui.GuiButton;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiText;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.function.Consumer;

public class GuiSchematicList extends GuiElement {
    private static int pageLength = 8;

    private int page = 0;

    private UpgradeSchematic[] schematics;

    private Consumer<UpgradeSchematic> schematicSelectionConsumer;

    private GuiElement listGroup;

    private GuiButton buttonBack;
    private GuiButton buttonForward;

    private GuiText emptyStateText;

    public GuiSchematicList(int x, int y, Consumer<UpgradeSchematic> schematicSelectionConsumer) {
        super(x, y, 224, 67);

        listGroup = new GuiElement(3, 3, width - 6, height - 6);
        addChild(listGroup);

        buttonBack = new GuiButton(-25, height + 4, 45, 12, "< Previous", () -> setPage(getPage() - 1));
        addChild(buttonBack);
        buttonForward = new GuiButton(width - 20, height + 4, 30, 12, "Next >", () -> setPage(getPage() + 1));
        addChild(buttonForward);

        emptyStateText = new GuiText(10, 23, 204, ChatFormatting.GRAY + I18n.get("tetra.workbench.schematic_list.empty"));
        addChild(emptyStateText);

        this.schematicSelectionConsumer = schematicSelectionConsumer;
    }

    public void setSchematics(UpgradeSchematic[] schematics) {
        this.schematics = schematics;
        emptyStateText.setVisible(schematics.length == 0);
        setPage(0);
    }

    private void updateSchematics() {
        int offset = page * pageLength;
        int count = pageLength;

        if (count + offset > schematics.length) {
            count = schematics.length - offset;
        }

        listGroup.clearChildren();
        for (int i = 0; i < count; i++) {
            UpgradeSchematic schematic = schematics[i + offset];
            listGroup.addChild(new GuiSchematicListItem(
                    i / (pageLength / 2) * 109,
                    i % (pageLength / 2) * 14,
                    schematic, () -> schematicSelectionConsumer.accept(schematic)));
        }
    }

    private int getPage() {
        return page;
    }

    private void setPage(int page) {
        this.page = page;

        buttonBack.setVisible(page > 0);
        buttonForward.setVisible(page < getNumPages() - 1);
        updateSchematics();

    }

    private int getNumPages() {
        return (int) Math.ceil(1f * schematics.length / pageLength );
    }
}
