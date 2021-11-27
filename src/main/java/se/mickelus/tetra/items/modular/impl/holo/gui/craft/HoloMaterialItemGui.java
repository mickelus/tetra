package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import se.mickelus.mgui.gui.GuiClickable;
import se.mickelus.mgui.gui.GuiItem;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiItemRolling;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.module.data.MaterialData;

import java.util.function.Consumer;

public class HoloMaterialItemGui extends GuiClickable {
    protected GuiTexture backdrop;

    protected MaterialData material;
    protected Consumer<MaterialData> onHover;
    protected Consumer<MaterialData> onBlur;

    GuiItemRolling icon;

    protected boolean isMuted = false;

    public HoloMaterialItemGui(int x, int y, MaterialData material,
            Consumer<MaterialData> onHover, Consumer<MaterialData> onBlur, Consumer<MaterialData> onSelect) {
        super(x, y, 16, 16, () -> onSelect.accept(material));

        this.material = material;
        this.onHover = onHover;
        this.onBlur = onBlur;

        backdrop = new GuiTexture(0, 0, 16, 16, 52, 16, GuiTextures.workbench);
        addChild(backdrop);

        icon = new GuiItemRolling(0, 0)
                .setTooltip(false)
                .setCountVisibility(GuiItem.CountMode.never)
                .setItems(material.material.getApplicableItemStacks());
        addChild(icon);
    }

    public void updateSelection(MaterialData material) {
        isMuted = material != null && !this.material.equals(material);
        backdrop.setColor(isMuted ? GuiColors.muted : GuiColors.normal);
    }

    @Override
    protected void onFocus() {
        super.onFocus();

        onHover.accept(material);

        backdrop.setColor(GuiColors.hover);
    }

    @Override
    protected void onBlur() {
        super.onBlur();

        onBlur.accept(material);

        backdrop.setColor(isMuted ? GuiColors.muted : GuiColors.normal);
    }
}
