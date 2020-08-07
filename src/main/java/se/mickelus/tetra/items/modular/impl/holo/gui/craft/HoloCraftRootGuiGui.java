package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.client.resources.I18n;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.items.modular.impl.holo.gui.HoloRootBaseGui;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.LinkedList;

public class HoloCraftRootGuiGui extends HoloRootBaseGui {

    public static final char backBinding = 'q';

    private HoloBreadcrumbsGui breadcrumbs;
    private int depth = 0;

    private HoloItemsGui itemsView;
    private ModularItem item;

    private HoloSchematicsGui schematicsView;
    private String slot;

    private HoloSchematicGui schematicView;
    private UpgradeSchematic schematic;

    public HoloCraftRootGuiGui(int x, int y) {
        super(x, y);

        breadcrumbs = new HoloBreadcrumbsGui(0, 0, width, this::onBreadcrumbClick);
        breadcrumbs.setVisible(false);
        addChild(breadcrumbs);

        itemsView = new HoloItemsGui(0, 70, width, height, this::onItemSelect, this::onSlotSelect);
        addChild(itemsView);

        schematicsView = new HoloSchematicsGui(0, 20, width, height, this::onSchematicSelect);
        schematicsView.setVisible(false);
        addChild(schematicsView);

        schematicView = new HoloSchematicGui(0, 20, width, height);
        schematicView.setVisible(false);
        addChild(schematicView);
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

    private void onItemSelect(ModularItem item) {
        this.item = item;

        itemsView.changeItem(item);
        itemsView.setVisible(true);

        this.slot = null;
        schematicsView.setVisible(false);

        this.schematic = null;
        schematicView.setVisible(false);

        if (depth > 1) {
            itemsView.animateBack();
        }

        updateBreadcrumb();
    }

    private void onSlotSelect(String slot) {
        this.slot = slot;

        schematicsView.update(item, slot);
        schematicsView.setVisible(true);

        itemsView.setVisible(false);

        this.schematic = null;
        schematicView.setVisible(false);

        updateBreadcrumb();
    }

    private void onSchematicSelect(UpgradeSchematic schematic) {
        this.schematic = schematic;

        schematicView.update(item, slot, schematic);
        schematicView.setVisible(true);

        schematicsView.setVisible(false);

        itemsView.setVisible(false);

        updateBreadcrumb();
    }

    private void updateBreadcrumb() {
        breadcrumbs.setVisible(item != null);

        if (item != null) {
            LinkedList<String> result = new LinkedList<>();

            result.add(I18n.format("tetra.holo.craft.breadcrumb.root"));

            result.add(I18n.format("tetra.holo.craft." + item.getRegistryName().getPath()));

            if (slot != null) {
                result.add(getSlotName());
            }

            if (schematic != null) {
                result.add(schematic.getName());
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
                schematicsView.animateOpen();
                break;
            case 3:
                schematicView.animateOpen();
                break;
        }

        breadcrumbs.animateOpen(depth > 1);
    }
}
