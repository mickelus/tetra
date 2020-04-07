package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import se.mickelus.mgui.gui.*;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.module.schema.UpgradeSchema;

import java.util.function.Consumer;

public class GuiSchemaList extends GuiElement {
    private static int pageLength = 8;

    private int page = 0;

    private UpgradeSchema[] schemas;

    private Consumer<UpgradeSchema> schemaSelectionConsumer;

    private GuiElement listGroup;

    private GuiButton buttonBack;
    private GuiButton buttonForward;

    private GuiText emptyStateText;

    public GuiSchemaList(int x, int y, Consumer<UpgradeSchema> schemaSelectionConsumer) {
        super(x, y, 224, 67);

        listGroup = new GuiElement(3, 3, width - 6, height - 6);
        addChild(listGroup);

        buttonBack = new GuiButton(-25, height + 4, 45, 12, "< Previous", () -> setPage(getPage() - 1));
        addChild(buttonBack);
        buttonForward = new GuiButton(width - 20, height + 4, 30, 12, "Next >", () -> setPage(getPage() + 1));
        addChild(buttonForward);

        emptyStateText = new GuiText(10, 23, 204, TextFormatting.GRAY + I18n.format("workbench.schema_list.empty"));
        addChild(emptyStateText);

        this.schemaSelectionConsumer = schemaSelectionConsumer;
    }

    public void setSchemas(UpgradeSchema[] schemas) {
        this.schemas = schemas;
        emptyStateText.setVisible(schemas.length == 0);
        setPage(0);
    }

    private void updateSchemas() {
        int offset = page * pageLength;
        int count = pageLength;

        if (count + offset > schemas.length) {
            count = schemas.length - offset;
        }

        listGroup.clearChildren();
        for (int i = 0; i < count; i++) {
            UpgradeSchema schema = schemas[i + offset];
            listGroup.addChild(new GuiSchemaListItem(
                    i / (pageLength / 2) * 109,
                    i % (pageLength / 2) * 14,
                    schema, () -> schemaSelectionConsumer.accept(schema)));
        }
    }

    private int getPage() {
        return page;
    }

    private void setPage(int page) {
        this.page = page;

        buttonBack.setVisible(page > 0);
        buttonForward.setVisible(page < getNumPages() - 1);
        updateSchemas();

    }

    private int getNumPages() {
        return (int) Math.ceil(1f * schemas.length / pageLength );
    }
}
