package se.mickelus.tetra.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.CapabilityHelper;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemEffect;

import java.util.Random;

public class FortuneBonusFunction extends LootFunction {
    private RandomValueRange count;
    private int limit;

    private Capability requiredCapability;
    private int capabilityLevel = -1;

    public FortuneBonusFunction(LootCondition[] conditionsIn, RandomValueRange count, int limit,
            Capability requiredCapability, int capabilityLevel) {
        super(conditionsIn);
        this.count = count;
        this.limit = limit;
        this.requiredCapability = requiredCapability;
        this.capabilityLevel = capabilityLevel;
    }

    @Override
    public ItemStack apply(ItemStack itemStack, Random rand, LootContext context) {
        int fortuneLevel = 0;
        PlayerEntity player = (PlayerEntity) context.getKillerPlayer();

        if (player != null && requiredCapability != null) {
            ItemStack providingStack = CapabilityHelper.getProvidingItemStack(requiredCapability, capabilityLevel, player);
            if (!providingStack.isEmpty() && providingStack.getItem() instanceof ItemModular) {
                fortuneLevel = ((ItemModular) providingStack.getItem()).getEffectLevel(providingStack, ItemEffect.fortune);
            }
        }

        itemStack.grow(Math.round(fortuneLevel * count.generateFloat(rand)));

        if (limit != 0 && itemStack.getCount() > limit) {
            itemStack.setCount(limit);
        }

        return itemStack;
    }

    public static class Serializer extends LootFunction.Serializer<FortuneBonusFunction> {
        public Serializer()
        {
            super(new ResourceLocation("tetra:fortune_enchant"), FortuneBonusFunction.class);
        }

        public void serialize(JsonObject json, FortuneBonusFunction object, JsonSerializationContext serializationContext) {
            json.add("count", serializationContext.serialize(object.count));
            json.add("limit", serializationContext.serialize(object.limit));
            json.add("requiredCapability", serializationContext.serialize(object.requiredCapability));
            json.add("capabilityLevel", serializationContext.serialize(object.capabilityLevel));

        }

        public FortuneBonusFunction deserialize(JsonObject object, JsonDeserializationContext deserializationContext, LootCondition[] conditionsIn) {
            return new FortuneBonusFunction(
                    conditionsIn,
                    JsonUtils.deserializeClass(object, "count", deserializationContext, RandomValueRange.class),
                    JsonUtils.getInt(object, "limit", 0),
                    Capability.valueOf(JsonUtils.getString(object, "requiredCapability")),
                    JsonUtils.getInt(object, "capabilityLevel", -1));
        }
    }
}
