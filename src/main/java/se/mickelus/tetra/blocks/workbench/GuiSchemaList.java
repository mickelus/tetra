package se.mickelus.tetra.blocks.workbench;

import se.mickelus.tetra.gui.GuiButton;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.module.UpgradeSchema;

import java.util.function.Consumer;

public class GuiSchemaList extends GuiElement {

    private static int pageLength = 5;

    private int page = 0;

    private UpgradeSchema[] schemas;

    private Consumer<UpgradeSchema> schemaSelectionConsumer;

    private GuiElement listGroup;
    private GuiButton buttonBack;
    private GuiButton buttonForward;

    public GuiSchemaList(int x, int y) {
        super(x, y, 224, 64);

        listGroup = new GuiElement(0, 0, width, height);
        addChild(listGroup);

        buttonBack = new GuiButton(-4, height + 4, 12, 20, "Previous", () -> setPage(getPage() - 1));
        buttonForward = new GuiButton(width - 16, height + 4, 12, 20, "Next", () -> setPage(getPage() + 1));
    }

    public void setSchemas(UpgradeSchema[] schemas) {
        this.schemas = schemas;
        setPage(0);



        updateSchemas();
    }

    private void updateSchemas() {
        int start = page * pageLength;
        int end = page * pageLength + pageLength;

        if (end > schemas.length) {
            end = schemas.length;
        }

        listGroup.clearChildren();
        for (int i = start; i < end; i++) {
            UpgradeSchema schema = schemas[i];
            listGroup.addChild(new GuiButton(
                    i / pageLength * 112 + 3,
                    i % pageLength * 13 + 3,
                    80, 12,
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

    }

    private int getNumPages() {
        return schemas.length / ( pageLength * 2 );
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
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        drawRect(refX + x, refY + y, refX + x + width, refY + + y + height, 0xff000000);
        super.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY);
    }
}
