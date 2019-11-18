package se.mickelus.tetra.items;

import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.data.ModuleData;
import se.mickelus.tetra.module.data.TweakData;

public class BasicModule extends ItemModule {
    public BasicModule(String slotKey, String moduleKey) {
        super(slotKey, moduleKey);

        DataManager.moduleData.onReload(() -> data = DataManager.moduleData.getData(new ResourceLocation(TetraMod.MOD_ID, moduleKey)));
        ItemUpgradeRegistry.instance.registerModule(moduleKey, this);
    }

    public BasicModule(String slotKey, String moduleKey, String tweakKey) {
        this(slotKey, moduleKey);

        DataManager.tweakData.onReload(() -> tweaks = DataManager.tweakData.getData(new ResourceLocation(TetraMod.MOD_ID, tweakKey)));
    }
}
