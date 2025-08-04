package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.module.schematic.OutcomePreview;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HolosphereCraftState {
    ImmutableMap<String, ItemState> itemState = ImmutableMap.of();

    private String selectedItem = null;
    private String selectedSlot = null;
    private UpgradeSchematic selectedSchematic = null;
    private OutcomePreview selectedVariant = null;
    private boolean showingMaterials = false;

    private Runnable onNavigationChange;

    public HolosphereCraftState(Runnable onNavigationChange) {
        this.onNavigationChange = onNavigationChange;
    }

    public int getDepth() {
        if (showingMaterials) {
            return 1;
        }
        if (selectedVariant != null) {
            return 4;
        }
        if (selectedSchematic != null) {
            return 3;
        }
        if (selectedSlot != null) {
            return 2;
        }
        if (selectedItem != null) {
            return 1;
        }
        return 0;
    }

    public String getSelectedItem() {
        return selectedItem;
    }

    public String getSelectedSlot() {
        return selectedSlot;
    }

    public String getSelectedSlotName() {
        ItemState selectedItemState = getSelectedItemState();
        if (selectedItemState != null) {
            ItemStack itemStack = selectedItemState.itemData.getDefaultStack();
            String[] majorKeys = selectedItemState.itemData().getAsModularItem().getMajorModuleKeys(itemStack);

            for (int i = 0; i < majorKeys.length; i++) {
                if (majorKeys[i].equals(selectedSlot)) {
                    return selectedItemState.itemData().getAsModularItem().getMajorModuleNames(itemStack)[i];
                }
            }

            String[] minorKeys = selectedItemState.itemData().getAsModularItem().getMinorModuleKeys(itemStack);

            for (int i = 0; i < minorKeys.length; i++) {
                if (minorKeys[i].equals(selectedSlot)) {
                    return selectedItemState.itemData().getAsModularItem().getMinorModuleNames(itemStack)[i];
                }
            }
        }
        return selectedSlot;
    }

    public UpgradeSchematic getSelectedSchematic() {
        return selectedSchematic;
    }

    public OutcomePreview getSelectedVariant() {
        return selectedVariant;
    }

    public boolean isShowingMaterials() {
        return showingMaterials;
    }

    public ImmutableMap<String, ItemState> getItemState() {
        return itemState;
    }

    @Nullable
    public ItemState getSelectedItemState() {
        if (selectedItem != null) {
            return itemState.get(selectedItem);
        }
        return null;
    }

    public void setAvailableItems(Map<String, HolosphereEntryData> entries) {
        this.itemState = entries.entrySet().stream()
                .collect(Collectors.collectingAndThen(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> new ItemState(entry.getValue(), entry.getValue().getDefaultStack())),
                        ImmutableMap::copyOf));
    }

    public List<HolosphereEntryData> getSortedItemData() {
        return itemState.values().stream()
                .map(ItemState::itemData)
                .sorted(Comparator.comparingInt(entry -> entry.position < 0 ? Integer.MAX_VALUE : entry.position))
                .toList();
    }

    public void onMaterialsSelect() {
        this.selectedItem = null;
        this.selectedSlot = null;
        this.selectedSchematic = null;
        this.showingMaterials = true;

        this.onNavigationChange.run();
    }

    public void onItemSelect(String item) {
        this.selectedItem = item;
        this.selectedSlot = null;
        this.selectedVariant = null;
        this.selectedSchematic = null;
        this.showingMaterials = false;

        this.onNavigationChange.run();
    }

    public void onSlotSelect(String slot) {
        this.selectedSlot = slot;
        this.selectedVariant = null;
        this.selectedSchematic = null;
        this.showingMaterials = false;

        this.onNavigationChange.run();
    }

    public void onSchematicSelect(UpgradeSchematic schematic) {
        this.selectedSchematic = schematic;
        this.selectedVariant = null;
        this.showingMaterials = false;

        this.onNavigationChange.run();
    }

    public void onVariantSelect(OutcomePreview variant) {
        this.selectedVariant = variant;
        this.showingMaterials = false;

        this.onNavigationChange.run();
    }

    public void openFromWorkbench(String key, ItemStack itemStack, String slot, UpgradeSchematic schematic) {
        this.selectedItem = key;
        this.selectedSlot = slot;
        this.selectedSchematic = schematic;

        // todo: enable this for templating feature
        // this.getSelectedItemState().setWorkingStack(itemStack);

        this.onNavigationChange.run();
    }

    public class ItemState {
        HolosphereEntryData itemData;
        ItemStack workingStack;

        public ItemState(HolosphereEntryData itemData, ItemStack workingStack) {
            this.itemData = itemData;
            this.workingStack = workingStack;
        }

        public HolosphereEntryData itemData() {
            return itemData;
        }

        public void setWorkingStack(ItemStack workingStack) {
            this.workingStack = workingStack;
        }

        public ItemStack workingStack() {
            return workingStack;
        }
    }
}
