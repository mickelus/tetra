package se.mickelus.tetra.items.modular;

import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.data.ModuleData;
import se.mickelus.tetra.module.data.TweakData;

public class BasicModule extends ItemModule {

    public BasicModule(ResourceLocation identifier, ModuleData data) {
        super(data.slots[0], identifier.getPath());

        variantData = data.variants;

        if (data.tweakKey != null) {
            TweakData[] tweaks = DataManager.tweakData.getData(data.tweakKey);
            if (tweaks != null) {
                this.tweaks = tweaks;
            } else {
                this.tweaks = new TweakData[0];
            }
        }
    }
}
