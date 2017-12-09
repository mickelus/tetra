package se.mickelus.tetra.items.toolbelt.module;

import se.mickelus.tetra.module.ItemModuleMajor;

public class SingleStrapModule extends ItemModuleMajor<ModuleDataToolbelt> {
    public static final String key = "toolbelt/single_strap";
    public SingleStrapModule(String slotKey) {
        super(slotKey, key);
    }
}
