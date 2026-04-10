package se.mickelus.tetra.craftingeffect.condition;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.aspect.ItemAspect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;
import se.mickelus.tetra.module.schematic.requirement.IntegerPredicate;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class AspectCondition implements CraftingEffectCondition {
    ItemAspect aspect;
    IntegerPredicate level;
    boolean anySlot = false;

    @Override
    public boolean test(ResourceLocation[] unlocks, ItemStack upgradedStack, String slot, boolean isReplacing, Player player,
            ItemStack[] materials, Map<ItemAbility, Integer> tools, UpgradeSchematic schematic, Level world, BlockPos pos, BlockState blockState) {
        IModularItem item = (IModularItem) upgradedStack.getItem();
        if (anySlot) {
            return Arrays.stream(item.getMajorModules(upgradedStack))
                    .filter(Objects::nonNull)
                    .anyMatch(module -> hasAspect(upgradedStack, module));
        }

        return Optional.ofNullable(item.getModuleFromSlot(upgradedStack, slot))
                .flatMap(module -> CastOptional.cast(module, ItemModuleMajor.class))
                .map(module -> hasAspect(upgradedStack, module))
                .orElse(false);
    }

    private boolean hasAspect(ItemStack itemStack, ItemModuleMajor module) {
        if (level != null) {
            return level.test(module.getAspects(itemStack).getLevel(aspect));
        }
        return module.getAspects(itemStack).getLevel(aspect) > 0;
    }
}
