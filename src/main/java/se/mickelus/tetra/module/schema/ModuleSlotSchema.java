package se.mickelus.tetra.module.schema;

import net.minecraft.item.Item;
import se.mickelus.tetra.module.ItemModule;

public class ModuleSlotSchema extends BasicSchema {

    public ModuleSlotSchema(String key, ItemModule module, Item item) {
        super(key, module, item);
    }

    @Override
    public String getKey() {
        return key + "/" + module.getSlot();
    }
}
