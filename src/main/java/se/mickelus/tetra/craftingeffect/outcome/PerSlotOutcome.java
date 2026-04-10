package se.mickelus.tetra.craftingeffect.outcome;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class PerSlotOutcome implements CraftingEffectOutcome {
    CraftingEffectOutcome outcome;

    String[] slots;
    String[] exclude;

    boolean current = true;
    boolean major = true;
    boolean minor = true;

    @Override
    public boolean apply(ResourceLocation[] unlockedEffects, ItemStack upgradedStack, String targetSlot, boolean isReplacing, Player player,
            ItemStack[] preMaterials, Map<ItemAbility, Integer> tools, Level world, UpgradeSchematic schematic, BlockPos pos, BlockState blockState,
            boolean consumeResources, ItemStack[] postMaterials, float severity) {
        if (upgradedStack.getItem() instanceof IModularItem item) {
            AtomicBoolean result = new AtomicBoolean(false);
            Stream.concat(
                            major ? Arrays.stream(item.getMajorModuleKeys(upgradedStack)) : Stream.empty(),
                            minor ? Arrays.stream(item.getMinorModuleKeys(upgradedStack)) : Stream.empty())
                    .filter(slot -> slots == null || Arrays.asList(slots).contains(slot))
                    .filter(slot -> exclude == null || !Arrays.asList(exclude).contains(slot))
                    .filter(slot -> current || !slot.equals(targetSlot))
                    .filter(slot -> item.getModuleFromSlot(upgradedStack, slot) != null)
                    .forEach(slot -> {
                        if (outcome.apply(unlockedEffects, upgradedStack, slot, isReplacing, player, preMaterials, tools, world, schematic, pos, blockState, consumeResources, postMaterials, severity)) {
                            result.set(true);
                        }
                    });
            return result.get();
        }


        return false;
    }
}
