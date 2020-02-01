package se.mickelus.tetra.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.conditions.ILootCondition;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.items.ItemModular;

public class FortuneBonusCondition implements ILootCondition {
    private float chance;
    private float fortuneMultiplier;

    private Capability requiredCapability;
    private int capabilityLevel = -1;

    @Override
    public boolean test(LootContext context) {
        int fortuneLevel = 0;

        if (requiredCapability != null) {
            ItemStack toolStack = context.get(LootParameters.TOOL);

            if (toolStack != null && toolStack.getItem() instanceof ItemModular) {
                if (((ItemModular) toolStack.getItem()).getCapabilityLevel(toolStack, requiredCapability) > capabilityLevel) {
                    fortuneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, toolStack);
                }
            }
        } else {
            fortuneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, context.get(LootParameters.TOOL));
        }

        return context.getRandom().nextFloat() < this.chance + fortuneLevel * this.fortuneMultiplier;
    }

    public static class Serializer extends ILootCondition.AbstractSerializer<FortuneBonusCondition> {
        public Serializer() {
            super(new ResourceLocation("tetra:random_chance_with_fortune"), FortuneBonusCondition.class);
        }

        public void serialize(JsonObject json, FortuneBonusCondition value, JsonSerializationContext context) {
            DataManager.instance.gson.toJsonTree(value).getAsJsonObject().entrySet().forEach(entry -> json.add(entry.getKey(), entry.getValue()));
        }

        public FortuneBonusCondition deserialize(JsonObject json, JsonDeserializationContext context) {
            return DataManager.instance.gson.fromJson(json, FortuneBonusCondition.class);
        }
    }
}