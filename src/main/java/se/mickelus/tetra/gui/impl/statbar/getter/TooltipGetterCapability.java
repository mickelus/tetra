package se.mickelus.tetra.gui.impl.statbar.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.capabilities.Capability;

public class TooltipGetterCapability implements ITooltipGetter {

    private final IStatGetter levelGetter;
    private final IStatGetter efficiencyGetter;
    private final String localizationKey;

    public TooltipGetterCapability(Capability capability) {
        localizationKey = "stats." + capability + ".tooltip";

        levelGetter = new StatGetterCapabilityLevel(capability);
        efficiencyGetter = new StatGetterCapabilityEfficiency(capability);
    }


    @Override
    public String getTooltip(PlayerEntity player, ItemStack itemStack) {
        return I18n.format(localizationKey,
                (int) levelGetter.getValue(player, itemStack),
                String.format("%.2f", efficiencyGetter.getValue(player, itemStack)));
    }
}
