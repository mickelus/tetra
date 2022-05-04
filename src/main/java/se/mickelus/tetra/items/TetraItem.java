package se.mickelus.tetra.items;

import net.minecraft.world.item.Item;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TetraItem extends Item implements InitializableItem {
    public TetraItem(Properties properties) {
        super(properties);
    }
}
