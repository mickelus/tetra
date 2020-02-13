package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.items.modular.ItemModular;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.util.CastOptional;

public class StatGetterReach implements IStatGetter {

    private static final int baseValue = 5;

    public StatGetterReach() { }

    @Override
    public boolean shouldShow(PlayerEntity player, ItemStack currentStack, ItemStack previewStack) {
            return (currentStack.getItem() instanceof ItemModularHandheld || previewStack.getItem() instanceof ItemModularHandheld)
                    && (getValue(player, currentStack) != baseValue || getValue(player, previewStack) != baseValue);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack) {
        return baseValue + CastOptional.cast(itemStack.getItem(), ItemModularHandheld.class)
                .map(item -> item.getRangeModifier(itemStack))
                .orElse(0d);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .map(module -> module.getRangeModifier(itemStack))
                .orElse(0d);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot, String improvement) {
        return CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class))
                .map(module -> module.getImprovement(itemStack, improvement).range)
                .orElse(0f);
    }
}
