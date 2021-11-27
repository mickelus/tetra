package se.mickelus.tetra.items.modular.impl.crossbow;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShootableItem;
import se.mickelus.tetra.blocks.forged.chthonic.ChthonicExtractorBlock;

import java.util.function.Predicate;

public class ShootableDummyItem extends ShootableItem {

    public static final Predicate<ItemStack> ammoPredicate = ARROWS_OR_FIREWORKS
            .or(stack -> stack.getItem() == ChthonicExtractorBlock.item)
            .or(stack -> stack.getItem() == ChthonicExtractorBlock.usedItem);

    public ShootableDummyItem() {
        super(new Properties().maxStackSize(1));
    }

    @Override
    public Predicate<ItemStack> getAmmoPredicate() {
        return ammoPredicate;
    }

    /**
     * Get the predicate to match ammunition when searching the player's inventory, not their main/offhand
     */
    @Override
    public Predicate<ItemStack> getInventoryAmmoPredicate() {
        return ARROWS;
    }

    @Override
    public int func_230305_d_() {
        return 8;
    }
}
