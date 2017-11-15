package se.mickelus.tetra.module;


public abstract class ItemModuleMajor<T extends ModuleData> extends ItemModule<T> {

    public ItemModuleMajor(String slotKey, String moduleKey, String dataKey) {
        super(slotKey, moduleKey, dataKey);
    }
}
