package se.mickelus.tetra.craftingeffect.outcome;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class DestabilizeOutcome implements CraftingEffectOutcome {
    CraftingEffectOutcome[] outcomes = new CraftingEffectOutcome[0];

    @Override
    public boolean apply(ResourceLocation[] unlockedEffects, ItemStack upgradedStack, String slot, boolean isReplacing, Player player,
            ItemStack[] preMaterials, Map<ToolAction, Integer> tools, Level world, UpgradeSchematic schematic, BlockPos pos, BlockState blockState,
            boolean consumeResources, ItemStack[] postMaterials) {
        AtomicBoolean success = new AtomicBoolean(false);

        if (consumeResources && upgradedStack.getItem() instanceof IModularItem item) {
            ItemModule module = item.getModuleFromSlot(upgradedStack, slot);
            if (module != null) {
                float destabilizationChance = module.getDestabilizationChance(upgradedStack, 1);
                if (destabilizationChance > 0) {

                    do {
                        if (destabilizationChance > world.getRandom().nextFloat()) {
                            CraftingEffectOutcome outcome = outcomes[world.getRandom().nextInt(0, outcomes.length)];
                            if (outcome.apply(unlockedEffects, upgradedStack, slot, isReplacing, player, preMaterials, tools, world, schematic, pos, blockState, consumeResources, postMaterials)) {
                                success.set(true);
                            }
                        }

                        destabilizationChance--;
                    }
                    while (destabilizationChance > 1);
                }
            }
        }
        return success.get();
    }
}
