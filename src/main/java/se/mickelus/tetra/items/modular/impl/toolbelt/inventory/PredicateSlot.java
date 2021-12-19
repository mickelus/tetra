package se.mickelus.tetra.items.modular.impl.toolbelt.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
public class PredicateSlot extends Slot {

    protected Predicate<ItemStack> predicate;

    public PredicateSlot(Container inventory, int index, int x, int y, Predicate<ItemStack> predicate) {
        super(inventory, index, x, y);

        this.predicate = predicate;
    }

    @Override
    public boolean mayPlace(@Nullable ItemStack itemStack) {
        return itemStack != null && predicate.test(itemStack);
    }
}
