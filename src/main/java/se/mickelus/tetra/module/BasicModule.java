package se.mickelus.tetra.module;

import net.minecraft.resources.ResourceLocation;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.module.data.ModuleData;
import se.mickelus.tetra.module.data.TweakData;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class BasicModule extends ItemModule {

    public BasicModule(ResourceLocation identifier, ModuleData data) {
        super(data.slots[0], identifier.getPath());

        variantData = data.variants;

        if (data.tweakKey != null) {
            TweakData[] tweaks = DataManager.instance.tweakData.getData(data.tweakKey);
            if (tweaks != null) {
                this.tweaks = tweaks;
            } else {
                this.tweaks = new TweakData[0];
            }
        }
    }
}
