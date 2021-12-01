package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.items.modular.impl.holo.gui.scan.ScannerBarGui;

public class TooltipGetterScannerHorizontalRange implements ITooltipGetter {

    private final IStatGetter levelGetter;

    public TooltipGetterScannerHorizontalRange(IStatGetter levelGetter) {
        this.levelGetter = levelGetter;
    }

    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {


        return I18n.get("tetra.stats.holo.scannerHorizontalSpread.tooltip",
                String.format("%.0f", levelGetter.getValue(player, itemStack)),
                String.format("%.1f", ScannerBarGui.getDegreesPerUnit()));
    }
}
