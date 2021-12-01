package se.mickelus.tetra.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.properties.IToolProvider;

public class FortuneBonusCondition implements ILootCondition {

    public static final ResourceLocation identifier = new ResourceLocation(TetraMod.MOD_ID, "random_chance_with_fortune");
    public static final LootConditionType type = new LootConditionType(new Serializer());

    private float chance;
    private float fortuneMultiplier;

    private ToolType requiredTool;
    private int requiredToolLevel = -1;

    @Override
    public boolean test(LootContext context) {
        int fortuneLevel = 0;

        if (requiredTool != null) {
            ItemStack toolStack = context.getParamOrNull(LootParameters.TOOL);

            if (toolStack != null && toolStack.getItem() instanceof IToolProvider) {
                if (((IToolProvider) toolStack.getItem()).getToolLevel(toolStack, requiredTool) > requiredToolLevel) {
                    fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, toolStack);
                }
            }
        } else {
            ItemStack tool = context.getParamOrNull(LootParameters.TOOL);
            if (tool != null) {
                fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, context.getParamOrNull(LootParameters.TOOL));
            }
        }

        return context.getRandom().nextFloat() < this.chance + fortuneLevel * this.fortuneMultiplier;
    }

    @Override
    public LootConditionType getType() {
        return type;
    }

    public static class Serializer implements ILootSerializer<FortuneBonusCondition> {
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