package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.util.CastOptional;

public class StatGetterCapabilityEfficiency implements IStatGetter {

    private Capability capability;

    public StatGetterCapabilityEfficiency(Capability effect) {
        this.capability = effect;
    }

    @Override
    public double getValue(EntityPlayer player, ItemStack itemStack) {
        return CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .map(item -> item.getCapabilityEfficiency(itemStack, capability))
                .orElse(0f);
    }

    @Override
    public double getValue(EntityPlayer player, ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModule.class))
                .map(module -> module.getCapabilityEfficiency(itemStack, capability))
                .orElse(0f);
    }

    @Override
    public double getValue(EntityPlayer player, ItemStack itemStack, String slot, String improvement) {
        return CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class))
                .map(module -> module.getImprovement(itemStack, improvement))
                .map(improvementData -> improvementData.capabilities.getEfficiency(capability))
                .orElse(0f);
    }
}
