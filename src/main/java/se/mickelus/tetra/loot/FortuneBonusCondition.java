package se.mickelus.tetra.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.conditions.ILootCondition;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.CapabilityHelper;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemEffect;

public class FortuneBonusCondition implements ILootCondition {
    private float chance;
    private float fortuneMultiplier;

    private Capability requiredCapability;
    private int capabilityLevel = -1;

    @Override
    public boolean test(LootContext context) {
        int fortuneLevel = 0;
        Entity entity = context.get(LootParameters.THIS_ENTITY);

        if (entity != null && requiredCapability != null) {
            ItemStack itemStack = CapabilityHelper.getProvidingItemStack(requiredCapability, capabilityLevel, entity);
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemModular) {
                fortuneLevel = ((ItemModular) itemStack.getItem()).getEffectLevel(itemStack, ItemEffect.fortune);
            }
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