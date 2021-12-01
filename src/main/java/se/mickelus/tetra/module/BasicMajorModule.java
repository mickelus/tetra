package se.mickelus.tetra.module;

import net.minecraft.resources.ResourceLocation;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.module.data.ModuleData;
import se.mickelus.tetra.module.data.TweakData;
import se.mickelus.tetra.util.Filter;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
@ParametersAreNonnullByDefault
public class BasicMajorModule extends ItemModuleMajor {

    public BasicMajorModule(ResourceLocation identifier, ModuleData data) {
        super(data.slots[0], identifier.getPath());

        variantData = data.variants;

        renderLayer = data.renderLayer;

        if (data.improvements.length > 0) {
            improvements = Arrays.stream(data.improvements)
                    .map(rl -> rl.getPath().endsWith("/")
                            ? DataManager.improvementData.getDataIn(rl)
                            : Optional.ofNullable(DataManager.improvementData.getData(rl)).map(Collections::singletonList).orElseGet(Collections::emptyList))
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
            TweakData[] tweaks = DataManager.tweakData.getData(data.tweakKey);
            if (tweaks != null) {
                this.tweaks = tweaks;
            } else {
                this.tweaks = new TweakData[0];
            }
        }
    }
}
