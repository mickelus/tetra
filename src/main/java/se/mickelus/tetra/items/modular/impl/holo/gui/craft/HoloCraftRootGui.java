package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.impl.dynamic.DynamicModularItem;
import se.mickelus.tetra.items.modular.impl.holo.gui.HoloRootBaseGui;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.item.HoloItemsGui;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.material.HoloMaterialListGui;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.schematic.HoloSchematicGui;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.schematic.HoloSchematicListGui;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.OutcomePreview;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.LinkedList;
import java.util.Map;

@ParametersAreNonnullByDefault
public class HoloCraftRootGui extends HoloRootBaseGui {

    public static final char backBinding = 'q';

    private final HoloBreadcrumbsGui breadcrumbs;
    private final HoloItemsGui itemsView;
    private final HoloSchematicListGui schematicsView;
    private final HoloSchematicGui schematicView;
    private final HoloMaterialListGui materialsView;

    private final HolosphereCraftState state = new HolosphereCraftState(this::onNavigationChanged);


    public HoloCraftRootGui(int x, int y) {
        super(x, y);

        breadcrumbs = new HoloBreadcrumbsGui(0, 0, width, this::onBreadcrumbClick);
        addChild(breadcrumbs);

        itemsView = new HoloItemsGui(0, 70, width, height, this::onItemSelect, this::onSlotSelect, this::onMaterialsSelect);
        addChild(itemsView);

        schematicsView = new HoloSchematicListGui(0, 13, width, height, this::onSchematicSelect);
        schematicsView.setVisible(false);
        addChild(schematicsView);

        schematicView = new HoloSchematicGui(0, 20, width, height, this::onVariantSelect);
        schematicView.setVisible(false);
        addChild(schematicView);

        materialsView = new HoloMaterialListGui(0, 20, width, height);
        materialsView.setVisible(false);
        addChild(materialsView);

        HolosphereEntryStore.instance.setListener(this::onItemsLoaded);
        onItemsLoaded();
    }

    private void onNavigationChanged() {
        HolosphereCraftState.ItemState selectedItemState = state.getSelectedItemState();
        if (state.isShowingMaterials()) {
            itemsView.setVisible(false);
            schematicsView.setVisible(false);
            schematicView.setVisible(false);
            materialsView.setVisible(true);
        } else if (state.getSelectedVariant() != null) {
            schematicView.openVariant(state.getSelectedVariant());
            schematicView.setVisible(true);

            schematicsView.setVisible(false);
            itemsView.setVisible(false);
            materialsView.setVisible(false);
        } else if (state.getSelectedSchematic() != null && selectedItemState != null) {
            schematicView.update(selectedItemState.workingStack(), state.getSelectedSlot(), state.getSelectedSchematic());
            schematicView.openVariant(null);
            schematicView.setVisible(true);

            schematicsView.setVisible(false);
            itemsView.setVisible(false);
            materialsView.setVisible(false);
        } else if (state.getSelectedSlot() != null && selectedItemState != null) {
            schematicsView.update(selectedItemState.itemData().getAsModularItem(), state.getSelectedSlot());
            schematicsView.setVisible(true);

            itemsView.setVisible(false);
            schematicView.setVisible(false);
            materialsView.setVisible(false);
        } else {
            itemsView.changeItem(state.getSelectedItem());
            itemsView.setVisible(true);

            schematicsView.setVisible(false);
            schematicView.setVisible(false);
            materialsView.setVisible(false);

            if (state.getDepth() > 1) {
                itemsView.animateBack();
            }
        }


        updateBreadcrumb();
    }

    private void onItemsLoaded() {
        state.setAvailableItems(HolosphereEntryStore.instance.getEntries());
        itemsView.loadEntries(state.getSortedItemData());
        onItemSelect(null);
    }

    @Override
    public boolean onCharType(char character, int modifiers) {
        if (super.onCharType(character, modifiers)) {
            return true;
        }

        if (character == backBinding && state.getDepth() > 0) {
            onBreadcrumbClick(state.getDepth() - 1);
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
                if (!state.isShowingMaterials()) {
                    onItemSelect(state.getSelectedItem());
                }
                break;
            case 2:
                onSlotSelect(state.getSelectedSlot());
                break;
            case 3:
                onSchematicSelect(state.getSelectedSchematic());
                break;
        }
    }

    private void onMaterialsSelect() {
        state.onMaterialsSelect();
    }

    private void onItemSelect(@Nullable String item) {
        state.onItemSelect(item);
    }

    private void onSlotSelect(String slot) {
        state.onSlotSelect(slot);
    }

    private void onSchematicSelect(UpgradeSchematic schematic) {
        state.onSchematicSelect(schematic);
    }

    private void onVariantSelect(OutcomePreview variant) {
        state.onVariantSelect(variant);
    }

    public void openFromWorkbench(IModularItem item, ItemStack itemStack, @Nullable String slot, @Nullable UpgradeSchematic schematic) {
        String key = HolosphereEntryStore.instance.getEntries().entrySet().stream()
                .filter(entry -> entry.getValue().item.equals(item))
                .filter(entry -> entry.getValue().archetype != null || entry.getValue().archetype.equals(DynamicModularItem.getArchetypeKey(itemStack)))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
        if (key != null) {
            state.openFromWorkbench(key, itemStack, slot, schematic);

            if (slot != null && schematic != null) {
                onSchematicSelect(schematic);
            } else {
                onItemSelect(null);
            }
        } else {
            onItemSelect(null);
        }
        breadcrumbs.animateOpen(true);
    }

    private void updateBreadcrumb() {
        LinkedList<String> result = new LinkedList<>();

        if (state.getDepth() > 0) {
            if (state.getSelectedItem() != null) {
                result.add(I18n.get("tetra.holo.craft.breadcrumb.root"));
                result.add(I18n.get("tetra.holo.craft." + state.getSelectedItemState().itemData().key));

                if (state.getSelectedSlot() != null) {
                    result.add(state.getSelectedSlotName());
                }

                if (state.getSelectedSchematic() != null) {
                    result.add(state.getSelectedSchematic().getName());
                }

                if (state.getSelectedVariant() != null) {
                    result.add(state.getSelectedVariant().variantName);
                }
            } else if (state.isShowingMaterials()) {
                result.add(I18n.get("tetra.holo.craft.breadcrumb.root"));
                result.add(I18n.get("tetra.holo.craft.breadcrumb.materials"));
            }
            breadcrumbs.setVisible(true);
            breadcrumbs.setItems(result.toArray(new String[0]));
        } else {
            breadcrumbs.setVisible(false);
        }
    }


    public void animateOpen() {
        switch (state.getDepth()) {
            case 0:
                itemsView.animateOpenAll();
                break;
            case 1:
                if (state.isShowingMaterials()) {
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

        breadcrumbs.animateOpen(state.getDepth() > 1);
    }

    @Override
    public void onReload() {
        if (state.getSelectedSchematic() != null) {
            schematicView.setVisible(false);
            UpgradeSchematic newSchematic = SchematicRegistry.getSchematic(state.getSelectedSchematic().getKey());
            onSchematicSelect(newSchematic);
        } else if (state.getSelectedSlot() != null) {
            onSlotSelect(state.getSelectedSlot());
        }

        materialsView.reload();
    }
}
