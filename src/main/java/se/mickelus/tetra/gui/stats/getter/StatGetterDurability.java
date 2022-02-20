package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class StatGetterDurability implements IStatGetter {

    public StatGetterDurability() {
    }

    @Override
    public double getValue(Player player, ItemStack itemStack) {
        return itemStack.getMaxDamage();
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .map(module -> module.getProperties(itemStack))
                .map(data -> data.durability
                        + (data.durabilityMultiplier != 0 ? (int) ((data.durabilityMultiplier - 1) * itemStack.getMaxDamage()) : 0))
                .orElse(0);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot, String improvement) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class))
                .map(module -> module.getImprovement(itemStack, improvement))
                .map(data -> data.durability
                        + (data.durabilityMultiplier != 0 ? (int) ((data.durabilityMultiplier - 1) * itemStack.getMaxDamage()) : 0))
                .orElse(0);
    }
}
