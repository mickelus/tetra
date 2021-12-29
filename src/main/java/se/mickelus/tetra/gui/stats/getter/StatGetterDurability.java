package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.util.CastOptional;

public class StatGetterDurability implements IStatGetter {

    public StatGetterDurability() { }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack) {
        return itemStack.getMaxDamage();
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .map(module -> module.getDurability(itemStack))
                .orElse(0);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot, String improvement) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class))
                .map(module -> {
                    ImprovementData data = module.getImprovement(itemStack, improvement);
                    return data != null
                            ? data.durability + (int)((data.durabilityMultiplier - 1) * module.getDurability(itemStack))
                            : 0;
                })
                .orElse(0);
    }
}
