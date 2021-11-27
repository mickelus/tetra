package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.impl.ModularBladedItem;
import se.mickelus.tetra.items.modular.impl.ModularDoubleHeadedItem;
import se.mickelus.tetra.items.modular.impl.ModularSingleHeadedItem;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;
import se.mickelus.tetra.items.modular.impl.crossbow.ModularCrossbowItem;
import se.mickelus.tetra.items.modular.impl.shield.ModularShieldItem;
import se.mickelus.tetra.items.modular.impl.toolbelt.ModularToolbeltItem;

import java.util.function.Consumer;

public class HoloItemsGui extends GuiElement {

    private final HoloSeparatorsGui separators;

    private final HoloMaterialsButtonGui materialsButton;

    private KeyframeAnimation openAnimation;
    private KeyframeAnimation backAnimation;

    public HoloItemsGui(int x, int y, int width, int height, Consumer<IModularItem> onItemSelect, Consumer<String> onSlotSelect, Runnable onMaterialsClick) {
        super(x, y, width, height);


        separators = new HoloSeparatorsGui(1, -71, width, height);
        addChild(separators);

        addChild(new HoloItemGui(-39, 0, ModularBladedItem.instance, 0,
                () -> onItemSelect.accept(ModularBladedItem.instance), onSlotSelect)
            .setAttachment(GuiAttachment.topCenter));

        addChild(new HoloItemGui(1, -40, ModularToolbeltItem.instance, 4,
                () -> onItemSelect.accept(ModularToolbeltItem.instance), onSlotSelect)
            .setAttachment(GuiAttachment.topCenter));

        addChild(new HoloItemGui(41, 0, ModularDoubleHeadedItem.instance, 1,
                () -> onItemSelect.accept(ModularDoubleHeadedItem.instance), onSlotSelect)
            .setAttachment(GuiAttachment.topCenter));

        if (ConfigHandler.enableSingle.get()) {
            addChild(new HoloItemGui(81, -40, ModularSingleHeadedItem.instance, 2,
                    () -> onItemSelect.accept(ModularSingleHeadedItem.instance), onSlotSelect)
                    .setAttachment(GuiAttachment.topCenter));
        }

        if (ConfigHandler.enableCrossbow.get()) {
            addChild(new HoloItemGui(-79, 40, ModularCrossbowItem.instance, 7,
                    () -> onItemSelect.accept(ModularCrossbowItem.instance), onSlotSelect)
                    .setAttachment(GuiAttachment.topCenter));
        }

        if (ConfigHandler.enableBow.get()) {
            addChild(new HoloItemGui(-79, -40, ModularBowItem.instance, 5,
                    () -> onItemSelect.accept(ModularBowItem.instance), onSlotSelect)
                    .setAttachment(GuiAttachment.topCenter));
        }

//        addChild(new GuiJournalItem(-79, -40, ModularBowItem.instance, 5,
//                () -> onItemSelect.accept(ModularBowItem.instance), onSlotSelect)
//                .setAttachment(GuiAttachment.topCenter));
//

        if (ConfigHandler.enableShield.get()) {
            addChild(new HoloItemGui(81, 40, ModularShieldItem.instance, 3,
                    () -> onItemSelect.accept(ModularShieldItem.instance), onSlotSelect)
                    .setAttachment(GuiAttachment.topCenter));
        }
//
//        addChild(new GuiJournalItem(121, 0, ModularDoubleHeadedItem.instance, 3,
//                () -> onItemSelect.accept(ModularBowItem.instance), onSlotSelect)
//                .setAttachment(GuiAttachment.topCenter));
//
//        addChild(new GuiJournalItem(-119, 0, ModularDoubleHeadedItem.instance, 3,
//                () -> onItemSelect.accept(ModularBowItem.instance), onSlotSelect)
//                .setAttachment(GuiAttachment.topCenter));

        materialsButton = new HoloMaterialsButtonGui(0, 60, onMaterialsClick);
        materialsButton.setAttachment(GuiAttachment.topCenter);
        addChild(materialsButton);

        openAnimation = new KeyframeAnimation(200, this)
                .applyTo(new Applier.TranslateY(y - 4, y), new Applier.Opacity(0, 1))
                .withDelay(800);

        backAnimation = new KeyframeAnimation(100, this)
                .applyTo(new Applier.TranslateY(y - 4, y), new Applier.Opacity(0, 1));
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

    public void changeItem(IModularItem item) {
        getChildren(HoloItemGui.class).forEach(child -> child.onItemSelected(item));
        materialsButton.setVisible(item == null);

        if (item == null) {
            separators.animateReopen();
        } else {
            separators.setVisible(false);
        }
    }
}
