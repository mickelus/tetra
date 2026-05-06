package se.mickelus.tetra.craftingeffect.outcome;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@ParametersAreNonnullByDefault
public class RemoveImprovementOutcome implements CraftingEffectOutcome {
    String[] improvements;

    @Override
    public boolean apply(ResourceLocation[] unlockedEffects, ItemStack upgradedStack, String slot, boolean isReplacing, Player player,
            ItemStack[] preMaterials,
            Map<ItemAbility, Integer> tools, Level world, UpgradeSchematic schematic, BlockPos pos, BlockState blockState, boolean consumeResources,
            ItemStack[] postMaterials, float severity) {
        return CastOptional.cast(upgradedStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(upgradedStack, slot))
                .flatMap(module -> CastOptional.cast(module, ItemModuleMajor.class))
                .map(module -> {
                    boolean result = false;
                    for (String improvement : improvements) {
                        if (module.getImprovementLevel(upgradedStack, improvement) != -1) {
                            module.removeImprovement(upgradedStack, improvement);
                            result = true;
                        }
                    }
                    return result;
                })
                .orElse(false);
    }
}
