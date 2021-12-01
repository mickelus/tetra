package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.client.resources.language.I18n;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiString;
import se.mickelus.mgui.gui.GuiStringSmall;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.module.data.MaterialData;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class HoloMaterialGroupGui extends GuiElement {

    private final GuiElement materialsContainer;

    private final KeyframeAnimation labelAnimation;
    private final KeyframeAnimation[] itemAnimations;

    public HoloMaterialGroupGui(int x, int y, String category, List<MaterialData> materials, int offset,
            Consumer<MaterialData> onVariantHover, Consumer<MaterialData> onVariantBlur, Consumer<MaterialData> onVariantSelect) {
        super(x, y, 0, 50);

        GuiString label = new GuiStringSmall(0, 0, I18n.get("tetra.variant_category." + category + ".label"));
        label.setColor(GuiColors.muted);
        addChild(label);

        materialsContainer = new GuiElement(0, 8, width, height);
        addChild(materialsContainer);

        labelAnimation = new KeyframeAnimation(100, label)
                .applyTo(new Applier.Opacity(0, 1), new Applier.TranslateX(x - 5, x))
                .withDelay(40 * offset);

        int width = 0;

        itemAnimations = new KeyframeAnimation[materials.size()];
        for (int i = 0; i < materials.size(); i++) {
            MaterialData material = materials.get(i);
            HoloMaterialItemGui item = new HoloMaterialItemGui((i / 2) * 20, (i % 2) * 20, material,
                    onVariantHover, onVariantBlur, onVariantSelect);
            materialsContainer.addChild(item);

            itemAnimations[i] = new KeyframeAnimation(80, item)
                    .applyTo(new Applier.Opacity(0, 1),
                            new Applier.TranslateY(item.getY() - 5, item.getY()))
                    .withDelay(40 + 40 * (offset + i / 2));

            width = item.getX() + item.getWidth();

        }

        setWidth(Math.max(width, label.getWidth()));
    }

    public void updateSelection(MaterialData material) {
        materialsContainer.getChildren(HoloMaterialItemGui.class).forEach(variant -> variant.updateSelection(material));
    }

    public void animateIn() {
        labelAnimation.start();
        Arrays.stream(itemAnimations).forEach(KeyframeAnimation::start);
//        variantsContainer.getChildren(HoloDiagonalGui.class).forEach(HoloDiagonalGui::animateReopen);
    }
}
