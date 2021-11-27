package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.effect.ItemEffect;

public class StatGetterStability implements IStatGetter {

    private final StatGetterEffectLevel stabilizingGetter = new StatGetterEffectLevel(ItemEffect.stabilizing, 1);
    private final StatGetterEffectLevel unstableGetter = new StatGetterEffectLevel(ItemEffect.unstable, 1);


    public StatGetterStability() { }

    @Override
    public boolean shouldShow(PlayerEntity player, ItemStack currentStack, ItemStack previewStack) {
        return getValue(player, currentStack) != 0 || getValue(player, previewStack) != 0;
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack) {
        return stabilizingGetter.getValue(player, itemStack) - unstableGetter.getValue(player, itemStack);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot) {
        return stabilizingGetter.getValue(player, itemStack, slot) - unstableGetter.getValue(player, itemStack, slot);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot, String improvement) {
        return stabilizingGetter.getValue(player, itemStack, slot, improvement) - unstableGetter.getValue(player, itemStack, slot, improvement);
    }
}
