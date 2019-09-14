package se.mickelus.tetra.gui.impl.statbar.getter;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.util.CastOptional;

public class StatGetterCapabilityLevel implements IStatGetter {

    private Capability capability;

    public StatGetterCapabilityLevel(Capability effect) {
        this.capability = effect;
    }

    @Override
    public double getValue(EntityPlayer player, ItemStack itemStack) {
        return CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .map(item -> item.getCapabilityLevel(itemStack, capability))
                .orElse(0);
    }

    @Override
    public double getValue(EntityPlayer player, ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .map(module -> module.getCapabilityLevel(itemStack, capability))
                .orElse(0);
    }

    @Override
    public double getValue(EntityPlayer player, ItemStack itemStack, String slot, String improvement) {
        return CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class))
                .map(module -> module.getImprovement(itemStack, improvement))
                .map(improvementData -> improvementData.capabilities.getLevel(capability))
                .orElse(0);
    }
}
