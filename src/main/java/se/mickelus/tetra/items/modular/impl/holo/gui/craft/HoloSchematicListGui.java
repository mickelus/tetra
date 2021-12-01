package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.blocks.workbench.gui.GuiSchematicListItem;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.SchematicRarity;
import se.mickelus.tetra.module.schematic.SchematicType;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;
import se.mickelus.tetra.util.Filter;

import java.util.Arrays;
import java.util.function.Consumer;

public class HoloSchematicListGui extends GuiElement {

    private Consumer<UpgradeSchematic> onSchematicSelect;

    private KeyframeAnimation openAnimation;

    private KeyframeAnimation showAnimation;
    private KeyframeAnimation hideAnimation;

    public HoloSchematicListGui(int x, int y, int width, int height, Consumer<UpgradeSchematic> onSchematicSelect) {
        super(x, y, width, height);

        this.onSchematicSelect = onSchematicSelect;

        openAnimation = new KeyframeAnimation(80, this)
                .applyTo(new Applier.Opacity(0, 1), new Applier.TranslateY(y - 5, y))
                .withDelay(120);

        showAnimation = new KeyframeAnimation(80, this)
                .applyTo(new Applier.Opacity(1), new Applier.TranslateY(y));

        hideAnimation = new KeyframeAnimation(60, this)
                .applyTo(new Applier.Opacity(0), new Applier.TranslateY(y - 5))
                .onStop(complete -> this.isVisible = false);
    }

    public void update(IModularItem item, String slot) {
        int offset = 0;
        int pageLines = 8;
        UpgradeSchematic[] schematics = Arrays.stream(SchematicRegistry.getSchematics(slot, ItemStack.EMPTY))
                .filter(schematic -> !schematic.isHoning())
                .filter(schematic -> !schematic.getRarity().equals(SchematicRarity.temporary))
                .filter(schematic -> !schematic.getType().equals(SchematicType.improvement))
                .filter(Filter.distinct(UpgradeSchematic::getName))
                .toArray(UpgradeSchematic[]::new);

        int count = schematics.length;

        clearChildren();

        for (int i = 0; i < count; i++) {
            UpgradeSchematic schematic = schematics[i + offset];
            addChild(new GuiSchematicListItem(
                    i / pageLines * 106,
                    i % pageLines * 14,
                    103,
                    schematic, () -> onSchematicSelect.accept(schematic)));
        }
    }

    public void animateOpen() {
        openAnimation.start();
    }

    @Override
    protected void onShow() {
        super.onShow();
        hideAnimation.stop();
        showAnimation.start();
    }

    @Override
    protected boolean onHide() {
        showAnimation.stop();
        hideAnimation.start();

        return false;
    }
}
