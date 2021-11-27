package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.items.modular.impl.holo.gui.scan.ScannerBarGui;

public class TooltipGetterScannerHorizontalRange implements ITooltipGetter {

    private final IStatGetter levelGetter;

    public TooltipGetterScannerHorizontalRange(IStatGetter levelGetter) {
        this.levelGetter = levelGetter;
    }

    @Override
    public String getTooltipBase(PlayerEntity player, ItemStack itemStack) {


        return I18n.format("tetra.stats.holo.scannerHorizontalSpread.tooltip",
                String.format("%.0f", levelGetter.getValue(player, itemStack)),
                String.format("%.1f", ScannerBarGui.getDegreesPerUnit()));
    }
}
