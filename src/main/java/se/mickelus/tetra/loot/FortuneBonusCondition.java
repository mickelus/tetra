package se.mickelus.tetra.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.CapabilityHelper;
import se.mickelus.tetra.data.DataHandler;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemEffect;

import java.util.Random;

public class FortuneBonusCondition implements LootCondition {
    private float chance;
    private float fortuneMultiplier;

    private Capability requiredCapability;
    private int capabilityLevel = -1;

    @Override
    public boolean testCondition(Random rand, LootContext context) {
        int fortuneLevel = 0;
        EntityPlayer player = (EntityPlayer) context.getKillerPlayer();

        if (player != null && requiredCapability != null) {
            ItemStack itemStack = CapabilityHelper.getProvidingItemStack(requiredCapability, capabilityLevel, player);
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemModular) {
                fortuneLevel = ((ItemModular) itemStack.getItem()).getEffectLevel(itemStack, ItemEffect.fortune);
            }
        }

        return rand.nextFloat() < this.chance + fortuneLevel * this.fortuneMultiplier;
    }

    public static class Serializer extends LootCondition.Serializer<FortuneBonusCondition> {
        public Serializer() {
            super(new ResourceLocation("tetra:random_chance_with_fortune"), FortuneBonusCondition.class);
        }

        public void serialize(JsonObject json, FortuneBonusCondition value, JsonSerializationContext context) {
            DataHandler.instance.gson.toJsonTree(value).getAsJsonObject().entrySet().forEach(entry -> json.add(entry.getKey(), entry.getValue()));
        }

        public FortuneBonusCondition deserialize(JsonObject json, JsonDeserializationContext context) {
            return DataHandler.instance.gson.fromJson(json, FortuneBonusCondition.class);
        }
    }
}
