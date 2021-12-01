package se.mickelus.tetra.craftingeffect;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.craftingeffect.condition.CraftTypeCondition;
import se.mickelus.tetra.craftingeffect.condition.CraftingEffectCondition;
import se.mickelus.tetra.craftingeffect.condition.LockedCondition;
import se.mickelus.tetra.craftingeffect.condition.MaterialCondition;
import se.mickelus.tetra.craftingeffect.outcome.ApplyImprovementOutcome;
import se.mickelus.tetra.craftingeffect.outcome.CraftingEffectOutcome;
import se.mickelus.tetra.data.DataManager;

import java.util.HashMap;
import java.util.Map;

public class CraftingEffectRegistry {
    protected Map<String, Class<? extends CraftingEffectCondition>> conditionTypes = new HashMap<>();
    Map<String, Class<? extends CraftingEffectOutcome>> effectTypes = new HashMap<>();

    public static CraftingEffectRegistry instance;

    public CraftingEffectRegistry() {
        instance = this;
    }

    public static void registerConditionType(String identifier, Class<? extends CraftingEffectCondition> clazz) {
        instance.conditionTypes.put(identifier, clazz);
    }

    public static Class<? extends CraftingEffectCondition> getConditionClass(String identifier) {
        return instance.conditionTypes.get(identifier);
    }

    public static void registerEffectType(String identifier, Class<? extends CraftingEffectOutcome> clazz) {
        instance.effectTypes.put(identifier, clazz);
    }

    public static Class<? extends CraftingEffectOutcome> getEffectClass(String identifier) {
        return instance.effectTypes.get(identifier);
    }

    public static CraftingEffect[] getEffects(ResourceLocation[] unlocks, ItemStack upgradedStack, String slot, boolean isReplacing, Player player,
            ItemStack[] materials, Map<ToolType, Integer> tools, Level world, BlockPos pos, BlockState blockState) {
        return DataManager.craftingEffectData.getData().values().stream()
                .filter(effect -> effect.isApplicable(unlocks, upgradedStack, slot, isReplacing, player, materials, tools, world, pos, blockState))
                .toArray(CraftingEffect[]::new);
    }
}
