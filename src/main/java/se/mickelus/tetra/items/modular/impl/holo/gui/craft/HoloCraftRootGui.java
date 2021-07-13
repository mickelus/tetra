package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.client.resources.I18n;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.impl.holo.gui.HoloRootBaseGui;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.LinkedList;

public class HoloCraftRootGui extends HoloRootBaseGui {

    public static final char backBinding = 'q';

    private HoloBreadcrumbsGui breadcrumbs;
    private int depth = 0;

    private HoloItemsGui itemsView;
    private IModularItem item;

    private HoloSchematicsGui schematicsView;
    private String slot;

    private HoloSchematicGui schematicView;
    private UpgradeSchematic schematic;

    private boolean showingMaterials = false;
    private HoloMaterialsGui materialsView;

    public HoloCraftRootGui(int x, int y) {
        super(x, y);

        breadcrumbs = new HoloBreadcrumbsGui(0, 0, width, this::onBreadcrumbClick);
        breadcrumbs.setVisible(false);
        addChild(breadcrumbs);

        itemsView = new HoloItemsGui(0, 70, width, height, this::onItemSelect, this::onSlotSelect, this::onMaterialsSelect);
        addChild(itemsView);

        schematicsView = new HoloSchematicsGui(0, 20, width, height, this::onSchematicSelect);
        schematicsView.setVisible(false);
        addChild(schematicsView);

        schematicView = new HoloSchematicGui(0, 20, width, height);
        schematicView.setVisible(false);
        addChild(schematicView);

        materialsView = new HoloMaterialsGui(0, 20, width, height);
        materialsView.setVisible(false);
        addChild(materialsView);
    }

    @Override
    public boolean onCharType(char character, int modifiers) {
        if (super.onCharType(character, modifiers)) {
            return true;
        }

        if (character == backBinding && depth > 0) {
            onBreadcrumbClick(depth - 1);
            return true;
        }

        return false;
    }

    private void onBreadcrumbClick(int depth) {
        switch (depth) {
            case 0:
                onItemSelect(null);
                break;
            case 1:
                if (!showingMaterials) {
                    onItemSelect(item);
                }
                break;
            case 2:
                onSlotSelect(slot);
        }

        this.depth = depth;
    }

    private void onMaterialsSelect() {
        item = null;
        itemsView.setVisible(false);

        this.slot = null;
        schematicsView.setVisible(false);

        this.schematic = null;
        schematicView.setVisible(false);

        this.showingMaterials = true;
        materialsView.setVisible(true);

        updateBreadcrumb();
    }

    private void onItemSelect(IModularItem item) {
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

        this.showingMaterials = false;
        materialsView.setVisible(false);

        updateBreadcrumb();
    }

    private void onSlotSelect(String slot) {
        this.slot = slot;

        schematicsView.update(item, slot);
        schematicsView.setVisible(true);

        itemsView.setVisible(false);

        this.schematic = null;
        schematicView.setVisible(false);

        this.showingMaterials = false;
        materialsView.setVisible(false);

        updateBreadcrumb();
    }

    private void onSchematicSelect(UpgradeSchematic schematic) {
        this.schematic = schematic;

        schematicView.update(item, slot, schematic);
        schematicView.setVisible(true);

        schematicsView.setVisible(false);

        itemsView.setVisible(false);

        this.showingMaterials = false;
        materialsView.setVisible(false);

        updateBreadcrumb();
    }

    private void updateBreadcrumb() {
        breadcrumbs.setVisible(item != null);
        LinkedList<String> result = new LinkedList<>();

        if (item != null) {
            result.add(I18n.format("tetra.holo.craft.breadcrumb.root"));

            result.add(I18n.format("tetra.holo.craft." + item.getItem().getRegistryName().getPath()));

            if (slot != null) {
                result.add(getSlotName());
            }

            if (schematic != null) {
                result.add(schematic.getName());
            }
        } else if (showingMaterials) {
            result.add(I18n.format("tetra.holo.craft.breadcrumb.root"));
            result.add(I18n.format("tetra.holo.craft.breadcrumb.materials"));
        }

        depth = result.size() - 1;
        breadcrumbs.setVisible(result.size() > 1);
        breadcrumbs.setItems(result.toArray(new String[0]));
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
                itemsView.animateOpenAll();
                break;
            case 1:
                if (showingMaterials) {
                    materialsView.animateOpen();
                } else {
                    itemsView.animateOpen();
                }
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

    @Override
    public void onReload() {
        if (schematic != null) {
            schematicView.setVisible(false);
            UpgradeSchematic newSchematic = SchematicRegistry.getSchematic(schematic.getKey());
            onSchematicSelect(newSchematic);
        } else if (slot != null) {
            onSlotSelect(slot);
        }

        materialsView.reload();
    }
}
