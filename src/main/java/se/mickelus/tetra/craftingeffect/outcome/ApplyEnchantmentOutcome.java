package se.mickelus.tetra.craftingeffect.outcome;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.aspect.TetraEnchantmentHelper;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@ParametersAreNonnullByDefault
public class ApplyEnchantmentOutcome implements CraftingEffectOutcome {
    Map<Enchantment, Integer> enchantments = new HashMap<>();
    boolean force = false;
    StackMode stacking = StackMode.max;

    @Override
    public boolean apply(ResourceLocation[] unlockedEffects, ItemStack upgradedStack, String slot, boolean isReplacing, Player player, ItemStack[] preMaterials,
            Map<ToolAction, Integer> tools, Level world, UpgradeSchematic schematic, BlockPos pos, BlockState blockState, boolean consumeResources, ItemStack[] postMaterials) {
        if (upgradedStack.getItem() instanceof IModularItem item && item.getModuleFromSlot(upgradedStack, slot) instanceof ItemModuleMajor module) {
            AtomicBoolean success = new AtomicBoolean(false);
            Map<Enchantment, Integer> currentEnchantments = EnchantmentHelper.getEnchantments(upgradedStack);
            enchantments.entrySet().stream()
                    .filter(entry -> acceptsEnchantment(upgradedStack, module, currentEnchantments.keySet(), entry.getKey(), entry.getValue()))
                    .forEach(entry -> {
                        int level = entry.getValue();
                        if (stacksEnchantment(upgradedStack, module, entry.getKey(), level)) {
                            currentEnchantments.put(entry.getKey(), stackLevel(currentEnchantments.get(entry.getKey()), level));
                            EnchantmentHelper.setEnchantments(currentEnchantments, upgradedStack);
                        } else {
                            TetraEnchantmentHelper.applyEnchantment(upgradedStack, slot, entry.getKey(), level);
                        }
                        success.set(true);
                    });
            return success.get();
        }
        return false;
    }

    protected int stackLevel(int currentLevel, int newLevel) {
        return switch (stacking) {
            case add -> currentLevel + newLevel;
            case stack -> newLevel == currentLevel
                    ? currentLevel + 1
                    : Math.max(currentLevel, newLevel);
            case max -> Math.max(currentLevel, newLevel);
            case replace -> newLevel;
        };
    }

    protected boolean stacksEnchantment(ItemStack itemStack, ItemModuleMajor module, Enchantment enchantment, int level) {
        Map<Enchantment, Integer> moduleEnchantments = module.getEnchantments(itemStack);
        if (moduleEnchantments.containsKey(enchantment)) {
            int currentLevel = moduleEnchantments.get(enchantment);
            return level >= currentLevel && currentLevel < enchantment.getMaxLevel();
        }
        return false;
    }

    protected boolean acceptsEnchantment(ItemStack itemStack, ItemModuleMajor module, Set<Enchantment> currentEnchantments, Enchantment enchantment, int level) {
        return (module.acceptsEnchantment(itemStack, enchantment, false) || force)
                && (stacksEnchantment(itemStack, module, enchantment, level) || EnchantmentHelper.isEnchantmentCompatible(currentEnchantments, enchantment));
    }

    enum StackMode {
        add,
        stack,
        max,
        replace
    }
}
