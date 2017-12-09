package se.mickelus.tetra.items.toolbelt.module;

import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ModuleData;

public class BeltModule extends ItemModule<ModuleDataToolbelt> {
    public static final String key = "toolbelt/belt";

    public BeltModule(String slotKey) {
        super(slotKey, key);
    }
}
