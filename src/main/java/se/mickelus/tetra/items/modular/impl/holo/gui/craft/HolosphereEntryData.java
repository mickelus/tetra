package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.impl.dynamic.DynamicModularItem;
import se.mickelus.tetra.module.data.GlyphData;

public class HolosphereEntryData {
    public String key;
    public Item item;
    public String archetype;
    public GlyphData icon;
    public int position = -1;

    public HolosphereEntryData() {
    }

    public IModularItem getAsModularItem() {
        return (IModularItem) item;
    }

    public ItemStack getDefaultStack() {
        if (archetype != null) {
            ItemStack itemStack = item.getDefaultInstance();
            DynamicModularItem.setArchetypeKey(itemStack, archetype);
            return itemStack;
        }
        return item.getDefaultInstance();
    }
}
