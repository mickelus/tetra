package se.mickelus.tetra.craftingeffect.condition;

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
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class ModuleCondition implements CraftingEffectCondition {
    String module;
    String variant;
    boolean anySlot = false;

    @Override
    public boolean test(ResourceLocation[] unlocks, ItemStack upgradedStack, String slot, boolean isReplacing, Player player,
            ItemStack[] materials, Map<ItemAbility, Integer> tools, UpgradeSchematic schematic, Level world, BlockPos pos, BlockState blockState) {
        IModularItem item = (IModularItem) upgradedStack.getItem();
        if (anySlot) {
            return Arrays.stream(item.getMajorModules(upgradedStack))
                    .filter(Objects::nonNull)
                    .anyMatch(module -> hasModule(upgradedStack, module));
        }

        return Optional.ofNullable(item.getModuleFromSlot(upgradedStack, slot))
                .flatMap(module -> CastOptional.cast(module, ItemModuleMajor.class))
                .map(module -> hasModule(upgradedStack, module))
                .orElse(false);
    }

    private boolean hasModule(ItemStack itemStack, ItemModuleMajor module) {
        if (variant != null && !variant.equals(module.getVariantData(itemStack).key)) {
            return false;
        }

        if (this.module != null && !this.module.equals(module.getKey())) {
            return false;
        }

        return true;
    }
}
