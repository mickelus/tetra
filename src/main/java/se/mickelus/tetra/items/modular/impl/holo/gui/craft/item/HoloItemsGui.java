package se.mickelus.tetra.items.modular.impl.holo.gui.craft.item;

import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.HolosphereEntryData;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.material.HoloMaterialsButtonGui;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class HoloItemsGui extends GuiElement {

    private final HoloSeparatorsGui separators;
    private final GuiElement itemContainer;
    private final HoloMaterialsButtonGui materialsButton;

    private final KeyframeAnimation openAnimation;
    private final KeyframeAnimation backAnimation;
    private final Consumer<String> onItemSelect;
    private final Consumer<String> onSlotSelect;

    public HoloItemsGui(int x, int y, int width, int height, Consumer<String> onItemSelect, Consumer<String> onSlotSelect,
            Runnable onMaterialsClick) {
        super(x, y, width, height);

        this.onItemSelect = onItemSelect;
        this.onSlotSelect = onSlotSelect;


        separators = new HoloSeparatorsGui(1, -71, width, height);
        addChild(separators);

        itemContainer = new GuiElement(0, 0, width, height);
        addChild(itemContainer);

        materialsButton = new HoloMaterialsButtonGui(0, 60, onMaterialsClick);
        materialsButton.setAttachment(GuiAttachment.topCenter);
        addChild(materialsButton);

        openAnimation = new KeyframeAnimation(200, this)
                .applyTo(new Applier.TranslateY(y - 4, y), new Applier.Opacity(0, 1))
                .withDelay(800);

        backAnimation = new KeyframeAnimation(100, this)
                .applyTo(new Applier.TranslateY(y - 4, y), new Applier.Opacity(0, 1));


    }

    public void loadEntries(List<HolosphereEntryData> itemData) {
        itemContainer.clearChildren();
        for (int i = 0; i < itemData.size(); i++) {
            itemContainer.addChild(createItem(itemData.get(i), i));
        }
    }

    private HoloItemGui createItem(HolosphereEntryData data, int index) {
        int x = -999;
        int y = -999;
        if (index == 0) {
            x = 1;
            y = -40;
        } else {
            int sideIndex = (index - 1) / 2;

            if (sideIndex % 3 == 0) {
                x = (sideIndex / 3) * 80 + 40;
                y = 0;
            } else {
                x = (sideIndex / 3) * 80 + 80;
                y = sideIndex % 2 == 0 ? -40 : 40;
            }


            x = index % 2 == 0 ? x : -x;
            y = index % 2 == 0 ? y : -y;
            x++;
        }
        HoloItemGui itemGui = new HoloItemGui(x, y, data, () -> this.onItemSelect.accept(data.key), this.onSlotSelect);
        itemGui.setAttachment(GuiAttachment.topCenter);
        return itemGui;
    }

    public void animateOpen() {
        openAnimation.start();
    }

    public void animateOpenAll() {
        openAnimation.start();
        separators.animateOpen();
    }

    public void animateBack() {
        backAnimation.start();
    }

    public void changeItem(@Nullable String key) {
        itemContainer.getChildren(HoloItemGui.class).forEach(child -> child.onItemSelected(key));
        materialsButton.setVisible(key == null);

        if (key == null) {
            separators.animateReopen();
        } else {
            separators.setVisible(false);
        }
    }
}
