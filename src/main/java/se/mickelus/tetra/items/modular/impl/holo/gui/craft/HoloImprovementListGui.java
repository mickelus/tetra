package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.mutil.gui.impl.GuiHorizontalLayoutGroup;
import se.mickelus.mutil.gui.impl.GuiHorizontalScrollable;
import se.mickelus.tetra.module.schematic.OutcomePreview;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
@ParametersAreNonnullByDefault
public class HoloImprovementListGui extends GuiElement {
    private final List<HoloImprovementGui> improvements;
    private final GuiHorizontalScrollable container;
    private final GuiHorizontalLayoutGroup[] groups;

    private KeyframeAnimation showAnimation;
    private KeyframeAnimation hideAnimation;

    private final Consumer<OutcomePreview> onVariantHover;
    private final Consumer<OutcomePreview> onVariantBlur;
    private final Consumer<OutcomeStack> onVariantSelect;

    private int originalY;

    public HoloImprovementListGui(int x, int y, int width, int height, Consumer<OutcomePreview> onVariantHover,
            Consumer<OutcomePreview> onVariantBlur, Consumer<OutcomeStack> onVariantSelect) {
        super(x, y, width, height);

        originalY = y;

        improvements = new ArrayList<>();

        container = new GuiHorizontalScrollable(0, 0, width, height).setGlobal(true);
        addChild(container);

        groups = new GuiHorizontalLayoutGroup[3];
        for (int i = 0; i < groups.length; i++) {
            groups[i] = new GuiHorizontalLayoutGroup(0, i * 32, 32, 8);
            container.addChild(groups[i]);
        }

        showAnimation = new KeyframeAnimation(60, this)
                .applyTo(new Applier.Opacity(1), new Applier.TranslateY(y))
                .withDelay(100);

        hideAnimation = new KeyframeAnimation(60, this)
                .applyTo(new Applier.Opacity(0), new Applier.TranslateY(y - 5))
                .onStop(complete -> {
                    if (complete) {
                        this.isVisible = false;
                    }
                });

        this.onVariantHover = onVariantHover;
        this.onVariantBlur = onVariantBlur;
        this.onVariantSelect = onVariantSelect;
    }

    public void updateSchematics(ItemStack baseStack, String slot, UpgradeSchematic[] schematics) {
        improvements.clear();

        for (GuiElement group : groups) {
            group.clearChildren();
            group.setWidth(0);
        }

        for (int i = 0; i < schematics.length; i++) {
            HoloImprovementGui improvement = new HoloImprovementGui(0, 0, schematics[i], baseStack, slot,
                    onVariantHover, onVariantBlur, onVariantSelect);

            improvements.add(improvement);

            GuiHorizontalLayoutGroup group = getNextGroup();
            group.addChild(improvement);
            group.forceLayout();
        }

        container.markDirty();
    }

    private GuiHorizontalLayoutGroup getNextGroup() {
        GuiHorizontalLayoutGroup next = groups[0];
        int width = next.getWidth();

        for (int i = 1; i < groups.length; i++) {
            if (groups[i].getWidth() < width) {
                next = groups[i];
                width = next.getWidth();
            }
        }

        return next;
    }

    public void show() {
        hideAnimation.stop();
        setVisible(true);
        showAnimation.start();

        container.setOffset(0);
    }

    public void hide() {
        showAnimation.stop();
        hideAnimation.start();
    }

    public void forceHide() {
        setY(originalY);
        setOpacity(0);
        setVisible(false);
    }

    public void updateSelection(ItemStack itemStack, List<OutcomeStack> selectedOutcomes) {
        improvements.forEach(improvement -> improvement.updateSelection(itemStack, selectedOutcomes));
    }
}
