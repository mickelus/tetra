package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.module.ItemEffect;

public class TooltipGetterCapability implements ITooltipGetter {

    private final IStatGetter levelGetter;
    private final IStatGetter efficiencyGetter;
    private final String localizationKey;

    private static final String strikingKey = "tetra.stats.capability.striking";
    private IStatGetter strikingGetter;

    private static final String sweepingKey = "tetra.stats.capability.sweeping";
    private IStatGetter sweepingGetter;

    public TooltipGetterCapability(Capability capability) {
        localizationKey = "tetra.stats." + capability + ".tooltip";

        levelGetter = new StatGetterCapabilityLevel(capability);
        efficiencyGetter = new StatGetterCapabilityEfficiency(capability);

        if (capability == Capability.axe) {
            strikingGetter = new StatGetterEffectLevel(ItemEffect.strikingAxe, 1);
        } else if (capability == Capability.pickaxe) {
            strikingGetter = new StatGetterEffectLevel(ItemEffect.strikingPickaxe, 1);
        } else if (capability == Capability.cut) {
            strikingGetter = new StatGetterEffectLevel(ItemEffect.strikingCut, 1);
        } else if (capability == Capability.shovel) {
            strikingGetter = new StatGetterEffectLevel(ItemEffect.strikingShovel, 1);
        }

        sweepingGetter = new StatGetterEffectLevel(ItemEffect.sweepingStrike, 1);
    }


    @Override
    public String getTooltip(PlayerEntity player, ItemStack itemStack) {
        String modifier = "";

        if (strikingGetter != null && strikingGetter.getValue(player, itemStack) > 0) {
            if (sweepingGetter.getValue(player, itemStack) > 0) {
                modifier = I18n.format(sweepingKey);
            } else {
                modifier = I18n.format(strikingKey);
            }
        }

        return I18n.format(localizationKey,
                modifier,
                (int) levelGetter.getValue(player, itemStack),
                String.format("%.2f", efficiencyGetter.getValue(player, itemStack)));
    }
}
