package se.mickelus.tetra.craftingeffect.outcome;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;
import se.mickelus.tetra.aspect.TetraEnchantmentHelper;
import se.mickelus.tetra.craftingeffect.StackMode;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@ParametersAreNonnullByDefault
public class ApplyEnchantmentOutcome implements CraftingEffectOutcome {
    Map<Enchantment, Integer> enchantments = new HashMap<>();
    boolean force = false;
    StackMode stacking = StackMode.max;

    @Override
    public boolean apply(ResourceLocation[] unlockedEffects, ItemStack upgradedStack, String slot, boolean isReplacing, Player player,
            ItemStack[] preMaterials,
            Map<ItemAbility, Integer> tools, Level world, UpgradeSchematic schematic, BlockPos pos, BlockState blockState, boolean consumeResources,
            ItemStack[] postMaterials, float severity) {
        if (upgradedStack.getItem() instanceof IModularItem item && item.getModuleFromSlot(upgradedStack, slot) instanceof ItemModuleMajor module) {
            AtomicBoolean success = new AtomicBoolean(false);
            enchantments.entrySet().stream()
                    .filter(entry -> acceptsEnchantment(upgradedStack, module,
                            EnchantmentHelper.getEnchantmentsForCrafting(upgradedStack).keySet(),
                            TetraEnchantmentHelper.getHolder(entry.getKey()), entry.getValue()))
                    .forEach(entry -> {
                        int level = entry.getValue();
                        Holder<Enchantment> enchantmentHolder = TetraEnchantmentHelper.getHolder(entry.getKey());
                        if (stacksEnchantment(upgradedStack, module, entry.getKey(), level)) {
                            int currentLevel = getModuleEnchantmentLevel(upgradedStack, module, entry.getKey());
                            TetraEnchantmentHelper.applyEnchantment(upgradedStack, slot, enchantmentHolder, stacking.evaluate(currentLevel, level));
                        } else {
                            TetraEnchantmentHelper.applyEnchantment(upgradedStack, slot, enchantmentHolder, level);
                        }
                        success.set(true);
                    });
            return success.get();
        }
        return false;
    }

    protected boolean stacksEnchantment(ItemStack itemStack, ItemModuleMajor module, Enchantment enchantment, int level) {
        int currentLevel = getModuleEnchantmentLevel(itemStack, module, enchantment);
        return currentLevel > 0 && level >= currentLevel && currentLevel < enchantment.getMaxLevel();
    }

    protected boolean acceptsEnchantment(ItemStack itemStack, ItemModuleMajor module, Collection<Holder<Enchantment>> currentEnchantments, Holder<Enchantment> enchantment,
            int level) {
        return (module.acceptsEnchantment(itemStack, enchantment, false) || force)
                && (stacksEnchantment(itemStack, module, enchantment.value(), level)
                || EnchantmentHelper.isEnchantmentCompatible(currentEnchantments, enchantment));
    }

    private int getModuleEnchantmentLevel(ItemStack itemStack, ItemModuleMajor module, Enchantment enchantment) {
        return TetraEnchantmentHelper.getEnchantmentKey(enchantment)
                .map(ResourceLocation::toString)
                .map(module.getEnchantmentsPrimitive(itemStack)::get)
                .orElse(0);
    }

}
