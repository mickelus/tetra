package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.items.modular.ItemModular;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;
import se.mickelus.tetra.items.modular.impl.shield.ModularShieldItem;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.util.CastOptional;

public class StatGetterSpeedShield implements IStatGetter {

    public StatGetterSpeedShield() { }

    @Override
    public boolean shouldShow(PlayerEntity player, ItemStack currentStack, ItemStack previewStack) {
        return currentStack.getItem() instanceof ModularShieldItem || previewStack.getItem() instanceof ModularShieldItem;
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack) {
        return CastOptional.cast(itemStack.getItem(), ModularShieldItem.class)
                .map(item -> item.getCooldownBase(itemStack))
                .orElse(0d);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .map(module -> module.getSpeedModifier(itemStack) + (module.getSpeedMultiplierModifier(itemStack) - 1) * getValue(player, itemStack))
                .orElse(0d);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot, String improvement) {
        return CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class))
                .map(module -> {
                    ImprovementData data = module.getImprovement(itemStack, improvement);
                    return data.attackSpeed + (data.attackSpeedMultiplier - 1) * module.getSpeedModifier(itemStack);
                })
                .orElse(0d);
    }
}
