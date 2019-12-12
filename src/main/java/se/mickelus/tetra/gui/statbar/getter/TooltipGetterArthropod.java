package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.module.ItemEffect;

public class TooltipGetterArthropod implements ITooltipGetter {

    private static final IStatGetter levelGetter = new StatGetterEnchantmentLevel(Enchantments.BANE_OF_ARTHROPODS, 1);

    public TooltipGetterArthropod() { }


    @Override
    public String getTooltip(PlayerEntity player, ItemStack itemStack) {
        return I18n.format("stats.arthropod.tooltip",
                String.format("%.2f", levelGetter.getValue(player, itemStack) * 2.5),
                String.format("%.2f", levelGetter.getValue(player, itemStack) * 0.5));
    }
}
