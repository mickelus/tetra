package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.util.CastOptional;

public class StatGetterSpeedNormalized implements IStatGetter {

    public StatGetterSpeedNormalized() { }

    @Override
    public boolean shouldShow(PlayerEntity player, ItemStack currentStack, ItemStack previewStack) {
        return (currentStack.getItem() instanceof ItemModularHandheld || previewStack.getItem() instanceof ItemModularHandheld)
                && (getValue(player, currentStack) != 0 || getValue(player, previewStack) != 0);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack) {
        return 2.4d + CastOptional.cast(itemStack.getItem(), ItemModularHandheld.class)
                .map(item -> item.getSpeedModifier(itemStack))
                .orElse(0d);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), ModularItem.class)
                .map(item ->item.getModuleFromSlot(itemStack, slot))
                .map(module -> module.getSpeedModifier(itemStack) + (module.getSpeedMultiplierModifier(itemStack) - 1) * getValue(player, itemStack))
                .orElse(0d);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot, String improvement) {
        return CastOptional.cast(itemStack.getItem(), ModularItem.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class))
                .map(module -> {
                    ImprovementData data = module.getImprovement(itemStack, improvement);
                    return data.damage + (data.damageMultiplier - 1) * module.getDamageModifier(itemStack);
                })
                .orElse(0d);
    }
}
