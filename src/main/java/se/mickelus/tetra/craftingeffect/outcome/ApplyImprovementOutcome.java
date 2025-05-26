package se.mickelus.tetra.craftingeffect.outcome;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.aspect.ItemAspect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;
import se.mickelus.tetra.util.StreamHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@ParametersAreNonnullByDefault
public class ApplyImprovementOutcome implements CraftingEffectOutcome {
    Map<String, Integer> improvements;
    se.mickelus.tetra.craftingeffect.StackMode stacking = se.mickelus.tetra.craftingeffect.StackMode.max;


    @Override
    public boolean apply(ResourceLocation[] unlockedEffects, ItemStack upgradedStack, String slot, boolean isReplacing, Player player,
            ItemStack[] preMaterials,
            Map<ToolAction, Integer> tools, Level world, UpgradeSchematic schematic, BlockPos pos, BlockState blockState, boolean consumeResources,
            ItemStack[] postMaterials) {
        return CastOptional.cast(upgradedStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(upgradedStack, slot))
                .flatMap(module -> CastOptional.cast(module, ItemModuleMajor.class))
                .map(module -> {
                    AtomicBoolean result = new AtomicBoolean(false);
                    for (Map.Entry<String, Integer> improvement : improvements.entrySet()) {
                        String key = improvement.getKey();
                        if (key.startsWith("#")) {
                            ItemAspect aspect = ItemAspect.get(key.substring(1));

                            Arrays.stream(module.getAcceptedImprovements(aspect))
                                    .collect(StreamHelper.toShuffledList())
                                    .stream()
                                    .map(k -> new ImprovementPair(k, stacking.evaluate(module.getImprovementLevel(upgradedStack, k), improvement.getValue())))
                                    .filter(pair -> module.acceptsImprovementLevel(pair.key, pair.level))
                                    .findFirst()
                                    .ifPresent(pair -> {
                                        module.addImprovement(upgradedStack, pair.key, pair.level);
                                        result.set(true);
                                    });

                        } else {
                            int level = stacking.evaluate(module.getImprovementLevel(upgradedStack, key), improvement.getValue());
                            if (module.acceptsImprovementLevel(key, level)) {
                                module.addImprovement(upgradedStack, key, level);
                                result.set(true);
                            }
                        }
                    }
                    return result.get();
                })
                .orElse(false);
    }

    record ImprovementPair(String key, int level) {
    }
}
