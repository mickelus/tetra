package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemEffect;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.util.CastOptional;

public class StatGetterUnbreaking implements IStatGetter {

    public StatGetterUnbreaking() { }

    @Override
    public double getValue(EntityPlayer player, ItemStack itemStack) {
        return CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .map(item -> item.getEffectLevel(itemStack, ItemEffect.unbreaking))
                .map(level -> 1 - 1d / (level + 1))
                .orElse(0d);
    }

    @Override
    public double getValue(EntityPlayer player, ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModule.class))
                .map(module -> module.getEffectLevel(itemStack, ItemEffect.unbreaking))
                .map(level -> 1 - 1d / (level + 1))
                .orElse(0d);
    }

    @Override
    public double getValue(EntityPlayer player, ItemStack itemStack, String slot, String improvement) {
        return CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class))
                .map(module -> module.getImprovement(itemStack, improvement))

                .map(data -> data.effects.getLevel(ItemEffect.unbreaking))
                .map(level -> 1 - 1d / (level + 1))
                .orElse(0d);
    }
}
