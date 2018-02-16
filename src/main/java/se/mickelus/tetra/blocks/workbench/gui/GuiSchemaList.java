package se.mickelus.tetra.blocks.workbench.gui;

import se.mickelus.tetra.gui.GuiButton;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.GuiTexture;
import se.mickelus.tetra.module.UpgradeSchema;

import java.util.function.Consumer;

public class GuiSchemaList extends GuiElement {

    private static final String WORKBENCH_TEXTURE = "textures/gui/workbench.png";

    private static int pageLength = 8;

    private int page = 0;

    private UpgradeSchema[] schemas;

    private Consumer<UpgradeSchema> schemaSelectionConsumer;

    private GuiElement listGroup;
    private GuiButton buttonBack;
    private GuiButton buttonForward;

    public GuiSchemaList(int x, int y) {
        super(x, y, 224, 64);

        addChild(new GuiTexture(0, 0, width, height, 0, 68, WORKBENCH_TEXTURE));

        listGroup = new GuiElement(5, 5, width - 10, height - 10);
        addChild(listGroup);

        buttonBack = new GuiButton(-25, height + 4, 45, 12, "< Previous", () -> setPage(getPage() - 1));
        addChild(buttonBack);
        buttonForward = new GuiButton(width - 20, height + 4, 30, 12, "Next >", () -> setPage(getPage() + 1));
        addChild(buttonForward);
    }

    public void setSchemas(UpgradeSchema[] schemas) {
        this.schemas = schemas;
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
            listGroup.addChild(new GuiButton(
                    i / (pageLength / 2) * 107,
                    i % (pageLength / 2) * 13,
                    107, 12,
                    schema.getName(), () -> onSchemaSelect(schema)));
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

    public void onSchemaSelect(UpgradeSchema schema) {
        if (schemaSelectionConsumer != null) {
            schemaSelectionConsumer.accept(schema);
        }
    }

    public void registerSelectHandler(Consumer<UpgradeSchema> schemaSelectionConsumer) {
        this.schemaSelectionConsumer = schemaSelectionConsumer;
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        super.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);
    }
}
