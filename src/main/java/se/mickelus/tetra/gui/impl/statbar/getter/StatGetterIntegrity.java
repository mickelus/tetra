package se.mickelus.tetra.gui.impl.statbar.getter;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.util.CastOptional;

public class StatGetterIntegrity implements IStatGetter {

    public StatGetterIntegrity() {}

    @Override
    public boolean shouldShow(EntityPlayer player, ItemStack currentStack, ItemStack previewStack) {
        return true;
    }

    @Override
    public double getValue(EntityPlayer player, ItemStack itemStack) {
        return ItemModular.getIntegrityGain(itemStack) + ItemModular.getIntegrityCost(itemStack);
    }

    @Override
    public double getValue(EntityPlayer player, ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModule.class))
                .map(module -> module.getIntegrityGain(itemStack) + module.getIntegrityCost(itemStack))
                .orElse(0);
    }

    @Override
    public double getValue(EntityPlayer player, ItemStack itemStack, String slot, String improvement) {
        return CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class))
                .map(module -> module.getImprovement(itemStack, improvement))
                .map(improvementData -> improvementData.integrity)
                .orElse(0);
    }
}
