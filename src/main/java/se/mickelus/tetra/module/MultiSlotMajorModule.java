package se.mickelus.tetra.module;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.util.Filter;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.module.data.ModuleData;
import se.mickelus.tetra.module.data.ModuleModel;
import se.mickelus.tetra.module.data.TweakData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
public class MultiSlotMajorModule extends ItemModuleMajor {

    protected String slotSuffix;

    protected String unlocalizedName;

    public MultiSlotMajorModule(ResourceLocation identifier, ModuleData data) {
        super(data.slots[0], identifier.getPath());

        slotSuffix = data.slotSuffixes[0];

        // strip the suffix from the unlocalized name
        unlocalizedName = identifier.getPath().substring(0, identifier.getPath().length() - data.slotSuffixes[0].length());

        renderLayer = data.renderLayer;

        variantData = data.variants;

        if (data.improvements.length > 0) {
            improvements = Arrays.stream(data.improvements)
                    .map(rl -> rl.getPath().endsWith("/")
                            ? DataManager.instance.improvementData.getDataIn(rl)
                            : Optional.ofNullable(DataManager.instance.improvementData.getData(rl)).map(Collections::singletonList).orElseGet(Collections::emptyList))
                    .flatMap(Collection::stream)
                    .filter(Objects::nonNull)
                    .flatMap(Arrays::stream)
                    .filter(Filter.distinct(improvement -> improvement.key + ":" + improvement.level))
                    .toArray(ImprovementData[]::new);

            settleMax = Arrays.stream(improvements)
                    .filter(improvement -> improvement.key.equals(settleImprovement))
                    .mapToInt(ImprovementData::getLevel)
                    .max()
                    .orElse(0);
        }

        if (data.tweakKey != null) {
            TweakData[] tweaks = DataManager.instance.tweakData.getData(data.tweakKey);
            if (tweaks != null) {
                this.tweaks = tweaks;
            } else {
                this.tweaks = new TweakData[0];
            }
        }
    }

    @Override
    public String getUnlocalizedName() {
        return unlocalizedName;
    }

    @Override
    protected ModuleModel[] getImprovementModels(ItemStack itemStack, int tint) {
        return super.getImprovementModels(itemStack, tint);
    }

    @Override
    public ModuleModel[] getModels(ItemStack itemStack) {
        return Arrays.stream(super.getModels(itemStack))
                .map(model -> new ModuleModel(model.type, new ResourceLocation(TetraMod.MOD_ID, model.location.getPath() + slotSuffix), model.tint))
                .toArray(ModuleModel[]::new);
    }
}
