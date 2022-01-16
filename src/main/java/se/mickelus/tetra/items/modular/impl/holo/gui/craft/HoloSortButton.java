package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiString;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.gui.stats.sorting.IStatSorter;
import se.mickelus.tetra.gui.stats.sorting.StatSorters;
import se.mickelus.tetra.module.schematic.OutcomePreview;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class HoloSortButton extends GuiElement {
    private final List<Component> tooltip;
    private final GuiString label;
    private final GuiTexture icon;
    private final HoloSortPopover popover;
    Consumer<IStatSorter> onSelect;

    public HoloSortButton(int x, int y, Consumer<IStatSorter> onSelect) {
        super(x, y, 36, 9);

        this.onSelect = onSelect;

        icon = new GuiTexture(0, 1, 9, 9, 233, 0, GuiTextures.workbench);
        addChild(icon);

        label = new GuiString(11, 0, width - 11, "");
        addChild(label);

        popover = new HoloSortPopover(0, 11, this::onSelect);
        addChild(popover);

        tooltip = Collections.singletonList(new TranslatableComponent("tetra.holo.craft.variants_sort"));
    }

    public void update(OutcomePreview[] previews) {
        this.label.setString(StatSorters.none.getName());
        if (previews.length > 0) {
            Player player = Minecraft.getInstance().player;

            popover.update(StatSorters.sorters.stream()
                    .filter(sorter -> Arrays.stream(previews).anyMatch(preview -> sorter.getWeight(player, preview.itemStack) > 0))
                    .toArray(IStatSorter[]::new));
        }

        popover.setVisible(false);
        icon.setColor(GuiColors.normal);
        label.setColor(GuiColors.normal);
    }

    private void onSelect(IStatSorter sorter) {
        String name = sorter.getName();
        this.label.setString(name.length() > 4 ? name.substring(0, 4) : name);
        icon.setColor(GuiColors.normal);
        label.setColor(GuiColors.normal);
        this.onSelect.accept(sorter);
    }

    @Override
    public boolean onMouseClick(int x, int y, int button) {
        if (hasFocus()) {
            togglePopover(!popover.isVisible());
            return true;
        }

        return super.onMouseClick(x, y, button);
    }

    @Override
    public List<Component> getTooltipLines() {
        if (hasFocus() && !popover.isVisible()) {
            return tooltip;
        }
        return null;
    }

    private void togglePopover(boolean visible) {
        icon.setColor(visible ? GuiColors.hover : GuiColors.normal);
        label.setColor(visible ? GuiColors.hover : GuiColors.normal);
        popover.setVisible(visible);
    }

    public boolean isBlockingFocus() {
        return popover.isVisible() && popover.hasFocus();
    }

    @Override
    public boolean onCharType(char character, int modifiers) {
        if (character == 's') {
            togglePopover(!popover.isVisible());
            return true;
        }

        return super.onCharType(character, modifiers);
    }


    public void reset() {
        this.label.setString(StatSorters.none.getName());
    }
}
