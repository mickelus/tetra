package se.mickelus.tetra.items.modular.impl.crossbow;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShootableItem;
import se.mickelus.tetra.blocks.forged.chthonic.ChthonicExtractorBlock;

import java.util.function.Predicate;

import net.minecraft.item.Item.Properties;

public class ShootableDummyItem extends ShootableItem {

    public static final Predicate<ItemStack> ammoPredicate = ARROW_OR_FIREWORK
            .or(stack -> stack.getItem() == ChthonicExtractorBlock.item)
            .or(stack -> stack.getItem() == ChthonicExtractorBlock.usedItem);

    public ShootableDummyItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public Predicate<ItemStack> getSupportedHeldProjectiles() {
        return ammoPredicate;
    }

    /**
     * Get the predicate to match ammunition when searching the player's inventory, not their main/offhand
     */
    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ARROW_ONLY;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 8;
    }
}
