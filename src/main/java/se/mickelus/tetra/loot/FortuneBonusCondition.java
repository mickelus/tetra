package se.mickelus.tetra.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.registries.RegistryObject;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.properties.IToolProvider;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class FortuneBonusCondition implements LootItemCondition {
    public static final String identifier = "random_chance_with_fortune";
    public static RegistryObject<LootItemConditionType> type;

    private final int requiredToolLevel = -1;
    private float chance;
    private float fortuneMultiplier;
    private ToolAction requiredTool;

    @Override
    public boolean test(LootContext context) {
        int fortuneLevel = 0;

        if (requiredTool != null) {
            ItemStack toolStack = context.getParamOrNull(LootContextParams.TOOL);

            if (toolStack != null && toolStack.getItem() instanceof IToolProvider) {
                if (((IToolProvider) toolStack.getItem()).getToolLevel(toolStack, requiredTool) > requiredToolLevel) {
                    fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, toolStack);
                }
            }
        } else {
            ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
            if (tool != null) {
                fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, context.getParamOrNull(LootContextParams.TOOL));
            }
        }

        return context.getRandom().nextFloat() < this.chance + fortuneLevel * this.fortuneMultiplier;
    }

    @Override
    public LootItemConditionType getType() {
        return type.get();
    }

    public static class ConditionSerializer implements Serializer<FortuneBonusCondition> {
        @Override
        public void serialize(JsonObject json, FortuneBonusCondition value, JsonSerializationContext context) {
            DataManager.gson.toJsonTree(value).getAsJsonObject().entrySet().forEach(entry -> json.add(entry.getKey(), entry.getValue()));
        }

        @Override
        public FortuneBonusCondition deserialize(JsonObject json, JsonDeserializationContext context) {
            return DataManager.gson.fromJson(json, FortuneBonusCondition.class);
        }
    }
}
