package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.util.CastOptional;

public class StatGetterIntegrity implements IStatGetter {

    public StatGetterIntegrity() {}

    @Override
    public boolean shouldShow(PlayerEntity player, ItemStack currentStack, ItemStack previewStack) {
        return true;
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack) {
        return ModularItem.getIntegrityGain(itemStack) - ModularItem.getIntegrityCost(itemStack);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), ModularItem.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .map(module -> module.getIntegrityGain(itemStack) - module.getIntegrityCost(itemStack))
                .orElse(0);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot, String improvement) {
        return CastOptional.cast(itemStack.getItem(), ModularItem.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class))
                .map(module -> module.getImprovement(itemStack, improvement))
                .map(improvementData -> improvementData.integrity)
                .orElse(0);
    }
}
