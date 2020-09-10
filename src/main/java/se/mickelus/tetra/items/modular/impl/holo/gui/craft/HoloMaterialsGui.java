package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.mgui.gui.impl.GuiHorizontalLayoutGroup;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.module.data.MaterialData;
import se.mickelus.tetra.module.schematic.OutcomePreview;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HoloMaterialsGui extends GuiElement {
    private GuiHorizontalLayoutGroup groups;

    private HoloMaterialDetailGui detail;

    private MaterialData selectedItem;
    private MaterialData hoveredItem;

    private KeyframeAnimation showAnimation;
    private KeyframeAnimation hideAnimation;

//    private final HoloSeparatorsGui separators;

    public HoloMaterialsGui(int x, int y, int width, int height) {
        super(x, y, width, height);

        groups = new GuiHorizontalLayoutGroup(0, 0, height, 12);
        addChild(groups);

        detail = new HoloMaterialDetailGui(0, 76, width);
        detail.setVisible(false);
        addChild(detail);

        updateGroups();
    }

    protected void updateGroups() {
        groups.clearChildren();

        Map<String, List<MaterialData>> result = DataManager.materialData.getData().values().stream()
                .collect(Collectors.groupingBy(data -> data.category, LinkedHashMap::new, Collectors.toList()));

        // some wonk needed to do staggered animations of variants
        int offset = 0;
        for (Map.Entry<String, List<MaterialData>> entry : result.entrySet()) {
            groups.addChild(new HoloMaterialGroupGui(0, 0, entry.getKey(), entry.getValue(), offset,
                    this::onItemHover, this::onItemBlur, this::onItemSelect));
            offset += entry.getValue().size();
        }
    }

    @Override
    protected void onShow() {
        groups.getChildren(HoloMaterialGroupGui.class).forEach(HoloMaterialGroupGui::animateIn);
    }

    public void animateOpen() {
        new KeyframeAnimation(200, this)
                .applyTo(new Applier.TranslateY(y - 4, y), new Applier.Opacity(0, 1))
                .withDelay(800)
//                .onStop(complete -> separators.animateOpen())
                .start();
    }

    public void animateBack() {
        new KeyframeAnimation(100, this)
                .applyTo(new Applier.TranslateY(y - 4, y), new Applier.Opacity(0, 1))
                .start();
    }

    private void onItemHover(MaterialData material) {
        hoveredItem = material;

        detail.update(selectedItem, hoveredItem);
    }

    private void onItemBlur(MaterialData material) {
        if (material.equals(hoveredItem)) {
            detail.update(selectedItem, null);
        }
    }

    private void onItemSelect(MaterialData material) {
        selectedItem = material;

        groups.getChildren(HoloMaterialGroupGui.class).forEach(group -> group.updateSelection(material));

        detail.update(selectedItem, hoveredItem);
    }
}
