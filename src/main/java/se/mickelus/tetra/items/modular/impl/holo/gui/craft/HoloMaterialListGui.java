package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.mutil.gui.impl.GuiHorizontalLayoutGroup;
import se.mickelus.mutil.gui.impl.GuiHorizontalScrollable;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.module.data.MaterialData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@ParametersAreNonnullByDefault
public class HoloMaterialListGui extends GuiElement {
    private GuiHorizontalScrollable groupsScroll;
    private GuiHorizontalLayoutGroup groups;

    private HoloMaterialDetailGui detail;

    private MaterialData selectedItem;
    private MaterialData hoveredItem;

    private KeyframeAnimation openAnimation;

    public HoloMaterialListGui(int x, int y, int width, int height) {
        super(x, y, width, height);

        groupsScroll = new GuiHorizontalScrollable(0, 0, width, 75).setGlobal(true);
        addChild(groupsScroll);
        groups = new GuiHorizontalLayoutGroup(0, 0, 75, 12);
        groupsScroll.addChild(groups);

        detail = new HoloMaterialDetailGui(0, 76, width);
        detail.setVisible(false);
        addChild(detail);

        openAnimation = new KeyframeAnimation(200, this)
                .applyTo(new Applier.TranslateY(y - 4, y), new Applier.Opacity(0, 1))
                .withDelay(800);

        updateGroups();
    }

    protected void updateGroups() {
        groups.clearChildren();

        boolean isDevelopment = ConfigHandler.development.get();
        Map<String, List<MaterialData>> result = DataManager.instance.materialData.getData().values().stream()
                .filter(data -> data.material != null)
                .filter(data -> !data.hidden)
                .filter(data -> isDevelopment || data.material.getApplicableItemStacks().length > 0)
                .collect(Collectors.groupingBy(data -> data.category, LinkedHashMap::new, Collectors.toList()));

        // some wonk needed to do staggered animations
        int offset = 0;
        for (Map.Entry<String, List<MaterialData>> entry : result.entrySet()) {
            groups.addChild(new HoloMaterialGroupGui(0, 0, entry.getKey(), entry.getValue(), offset,
                    this::onHover, this::onBlur, this::onSelect));
            offset += entry.getValue().size();
        }
        groupsScroll.markDirty();
    }

    @Override
    protected void onShow() {
        onHover(null);
        groups.getChildren(HoloMaterialGroupGui.class).forEach(HoloMaterialGroupGui::animateIn);
    }

    public void animateOpen() {
        openAnimation.start();
    }

    private void onHover(MaterialData material) {
        hoveredItem = material;

        detail.update(selectedItem, hoveredItem);
    }

    private void onBlur(MaterialData material) {
        if (material.equals(hoveredItem)) {
            detail.update(selectedItem, null);
        }
    }

    private void onSelect(MaterialData material) {
        selectedItem = material;

        groups.getChildren(HoloMaterialGroupGui.class).forEach(group -> group.updateSelection(material));

        detail.update(selectedItem, hoveredItem);
    }

    @Override
    public boolean onMouseClick(int x, int y, int button) {
        if (button == 1) {
            hoveredItem = null;
            onSelect(null);
            return true;
        }

        return super.onMouseClick(x, y, button);
    }

    public void reload() {
        updateGroups();
        if (isVisible()) {
            animateOpen();
        }
    }
}
