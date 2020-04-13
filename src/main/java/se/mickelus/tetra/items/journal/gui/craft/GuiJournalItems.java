package se.mickelus.tetra.items.journal.gui.craft;

import net.minecraft.item.Item;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.items.modular.ItemModular;
import se.mickelus.tetra.items.modular.impl.ModularDoubleHeadedItem;
import se.mickelus.tetra.items.modular.impl.ModularSingleHeadedItem;
import se.mickelus.tetra.items.modular.impl.ModularBladedItem;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;
import se.mickelus.tetra.items.modular.impl.toolbelt.ModularToolbeltItem;

import java.util.function.Consumer;

public class GuiJournalItems extends GuiElement {

    private final GuiJournalSeparators separators;

    public GuiJournalItems(int x, int y, int width, int height, Consumer<ItemModular> onItemSelect, Consumer<String> onSlotSelect) {
        super(x, y, width, height);


        separators = new GuiJournalSeparators(1, -71, width, height);
        addChild(separators);

        addChild(new GuiJournalItem(-39, 0, ModularBladedItem.instance, 0,
                () -> onItemSelect.accept(ModularBladedItem.instance), onSlotSelect)
            .setAttachment(GuiAttachment.topCenter));

        addChild(new GuiJournalItem(1, -40, ModularToolbeltItem.instance, 4,
                () -> onItemSelect.accept(ModularToolbeltItem.instance), onSlotSelect)
            .setAttachment(GuiAttachment.topCenter));

        addChild(new GuiJournalItem(41, 0, ModularDoubleHeadedItem.instance, 1,
                () -> onItemSelect.accept(ModularDoubleHeadedItem.instance), onSlotSelect)
            .setAttachment(GuiAttachment.topCenter));

        if (ConfigHandler.enableSingle.get()) {
            addChild(new GuiJournalItem(81, -40, ModularSingleHeadedItem.instance, 2,
                    () -> onItemSelect.accept(ModularSingleHeadedItem.instance), onSlotSelect)
                    .setAttachment(GuiAttachment.topCenter));
        }

//        addChild(new GuiJournalItem(81, 40, ModularDoubleHeadedItem.instance, 3,
//                () -> onItemSelect.accept(ModularBowItem.instance), onSlotSelect)
//                .setAttachment(GuiAttachment.topCenter));

        if (ConfigHandler.enableBow.get()) {
            addChild(new GuiJournalItem(1, 40, ModularBowItem.instance, 5,
                    () -> onItemSelect.accept(ModularBowItem.instance), onSlotSelect)
                    .setAttachment(GuiAttachment.topCenter));
        }

//        addChild(new GuiJournalItem(-79, -40, ModularBowItem.instance, 5,
//                () -> onItemSelect.accept(ModularBowItem.instance), onSlotSelect)
//                .setAttachment(GuiAttachment.topCenter));
//
//        addChild(new GuiJournalItem(-79, 40, ModularBowItem.instance, 3,
//                () -> onItemSelect.accept(ModularBowItem.instance), onSlotSelect)
//                .setAttachment(GuiAttachment.topCenter));
//
//        addChild(new GuiJournalItem(121, 0, ModularDoubleHeadedItem.instance, 3,
//                () -> onItemSelect.accept(ModularBowItem.instance), onSlotSelect)
//                .setAttachment(GuiAttachment.topCenter));
//
//        addChild(new GuiJournalItem(-119, 0, ModularDoubleHeadedItem.instance, 3,
//                () -> onItemSelect.accept(ModularBowItem.instance), onSlotSelect)
//                .setAttachment(GuiAttachment.topCenter));
    }

    public void animateOpen() {
        new KeyframeAnimation(200, this)
                .applyTo(new Applier.TranslateY(y - 4, y), new Applier.Opacity(0, 1))
                .withDelay(800)
                .onStop(complete -> separators.animateOpen())
                .start();
    }

    public void animateBack() {
        new KeyframeAnimation(100, this)
                .applyTo(new Applier.TranslateY(y - 4, y), new Applier.Opacity(0, 1))
                .start();
    }

    public void changeItem(Item item) {
        getChildren(GuiJournalItem.class).forEach(child -> child.onItemSelected(item));

        if (item == null) {
            separators.animateReopen();
        } else {
            separators.setVisible(false);
        }
    }
}
