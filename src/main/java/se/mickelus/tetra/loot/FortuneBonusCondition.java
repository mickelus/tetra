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
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.items.modular.ModularItem;

public class FortuneBonusCondition implements ILootCondition {

    public static final ResourceLocation identifier = new ResourceLocation(TetraMod.MOD_ID, "random_chance_with_fortune");
    public static final LootConditionType type = new LootConditionType(new Serializer());

    private float chance;
    private float fortuneMultiplier;

    private Capability requiredCapability;
    private int capabilityLevel = -1;

    @Override
    public boolean test(LootContext context) {
        int fortuneLevel = 0;

        if (requiredCapability != null) {
            ItemStack toolStack = context.get(LootParameters.TOOL);

            if (toolStack != null && toolStack.getItem() instanceof ModularItem) {
                if (((ModularItem) toolStack.getItem()).getCapabilityLevel(toolStack, requiredCapability) > capabilityLevel) {
                    fortuneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, toolStack);
                }
            }
        } else {
            fortuneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, context.get(LootParameters.TOOL));
        }

        return context.getRandom().nextFloat() < this.chance + fortuneLevel * this.fortuneMultiplier;
    }

    @Override
    public LootConditionType func_230419_b_() {
        return type;
    }

    public static class Serializer implements ILootSerializer<FortuneBonusCondition> {
        @Override
        public void func_230424_a_(JsonObject json, FortuneBonusCondition value, JsonSerializationContext context) {
            DataManager.instance.gson.toJsonTree(value).getAsJsonObject().entrySet().forEach(entry -> json.add(entry.getKey(), entry.getValue()));
        }

        @Override
        public FortuneBonusCondition func_230423_a_(JsonObject json, JsonDeserializationContext context) {
            return DataManager.instance.gson.fromJson(json, FortuneBonusCondition.class);
        }
    }
}