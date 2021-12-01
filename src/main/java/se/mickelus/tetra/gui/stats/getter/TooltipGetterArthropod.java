package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class TooltipGetterArthropod implements ITooltipGetter {

    private static final IStatGetter levelGetter = new StatGetterEnchantmentLevel(Enchantments.BANE_OF_ARTHROPODS, 1);

    public TooltipGetterArthropod() { }


    @Override
    public String getTooltipBase(PlayerEntity player, ItemStack itemStack) {
        return I18n.get("tetra.stats.arthropod.tooltip",
                String.format("%.2f", levelGetter.getValue(player, itemStack) * 2.5),
                String.format("%.2f", levelGetter.getValue(player, itemStack) * 0.5));
    }
}
