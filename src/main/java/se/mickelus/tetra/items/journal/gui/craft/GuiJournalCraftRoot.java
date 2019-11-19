package se.mickelus.tetra.items.journal.gui.craft;

import net.minecraft.client.resources.I18n;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.items.duplex_tool.ItemDuplexToolModular;
import se.mickelus.tetra.items.journal.GuiJournalRootBase;
import se.mickelus.tetra.items.sword.ItemSwordModular;
import se.mickelus.tetra.items.toolbelt.ItemToolbeltModular;
import se.mickelus.tetra.module.schema.UpgradeSchema;

import java.util.LinkedList;

public class GuiJournalCraftRoot extends GuiJournalRootBase {

    public static final char backBinding = 'q';

    private GuiJournalBreadcrumbs breadcrumbs;
    private int depth = 0;

    private GuiJournalItems itemsView;
    private ItemModular item;

    private GuiJournalSchemas schemasView;
    private String slot;

    private GuiJournalSchema schemaView;
    private UpgradeSchema schema;

    public GuiJournalCraftRoot(int x, int y) {
        super(x, y);

        breadcrumbs = new GuiJournalBreadcrumbs(0, 0, width, this::onBreadcrumbClick);
        breadcrumbs.setVisible(false);
        addChild(breadcrumbs);

        itemsView = new GuiJournalItems(0, 71, width, height, this::onItemSelect, this::onSlotSelect);
        addChild(itemsView);

        schemasView = new GuiJournalSchemas(0, 20, width, height, this::onSchemaSelect);
        schemasView.setVisible(false);
        addChild(schemasView);

        schemaView = new GuiJournalSchema(0, 20, width, height);
        schemaView.setVisible(false);
        addChild(schemaView);
    }

    @Override
    public void charTyped(char typedChar) {
        switch (typedChar) {
            case backBinding:
                if (depth > 0) {
                    onBreadcrumbClick(depth - 1);
                }
            default:
        }
    }

    private void onBreadcrumbClick(int depth) {
        switch (depth) {
            case 0:
                onItemSelect(null);
                break;
            case 1:
                onItemSelect(item);
                break;
            case 2:
                onSlotSelect(slot);
        }

        this.depth = depth;
    }

    private void onItemSelect(ItemModular item) {
        this.item = item;

        itemsView.changeItem(item);
        itemsView.setVisible(true);

        this.slot = null;
        schemasView.setVisible(false);

        this.schema = null;
        schemaView.setVisible(false);

        if (depth > 1) {
            itemsView.animateBack();
        }

        updateBreadcrumb();
    }

    private void onSlotSelect(String slot) {
        this.slot = slot;

        schemasView.update(item, slot);
        schemasView.setVisible(true);

        itemsView.setVisible(false);

        this.schema = null;
        schemaView.setVisible(false);

        updateBreadcrumb();
    }

    private void onSchemaSelect(UpgradeSchema schema) {
        this.schema = schema;

        schemaView.update(item, slot, schema);
        schemaView.setVisible(true);

        schemasView.setVisible(false);

        itemsView.setVisible(false);

        updateBreadcrumb();
    }

    private void updateBreadcrumb() {
        breadcrumbs.setVisible(item != null);

        if (item != null) {
            LinkedList<String> result = new LinkedList<>();

            result.add(I18n.format("journal.craft.breadcrumb.root"));

            if (item instanceof ItemSwordModular) {
                result.add(I18n.format("journal.craft.sword"));
            } else if (item instanceof ItemToolbeltModular) {
                result.add(I18n.format("journal.craft.toolbelt"));
            } else if (item instanceof ItemDuplexToolModular) {
                result.add(I18n.format("journal.craft.tool"));
            }

            if (slot != null) {
                result.add(getSlotName());
            }

            if (schema != null) {
                result.add(schema.getName());
            }

            depth = result.size() - 1;
            breadcrumbs.setItems(result.toArray(new String[0]));
        }
    }

    private String getSlotName() {
        if (item != null) {
            String[] majorKeys = item.getMajorModuleKeys();

            for (int i = 0; i < majorKeys.length; i++) {
                if (majorKeys[i].equals(slot)) {
                    return item.getMajorModuleNames()[i];
                }
            }

            String[] minorKeys = item.getMinorModuleKeys();

            for (int i = 0; i < minorKeys.length; i++) {
                if (minorKeys[i].equals(slot)) {
                    return item.getMinorModuleNames()[i];
                }
            }
        }
        return slot;
    }

    public void animateOpen() {
        switch (depth) {
            case 0:
            case 1:
                itemsView.animateOpen();
                break;
            case 2:
                schemasView.animateOpen();
                break;
            case 3:
                schemaView.animateOpen();
                break;
        }

        breadcrumbs.animateOpen(depth > 1);
    }
}
