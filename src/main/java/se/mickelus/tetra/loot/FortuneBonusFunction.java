package se.mickelus.tetra.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.ILootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.ILootFunction;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.CapabilityHelper;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemEffect;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class FortuneBonusFunction implements ILootFunction {
    protected final ILootCondition[] conditions;
    private final Predicate<LootContext> combinedConditions;

    private RandomValueRange count;
    private int limit;

    private Capability requiredCapability;
    private int capabilityLevel = -1;

    public FortuneBonusFunction(ILootCondition[] conditions, RandomValueRange count, int limit,
            Capability requiredCapability, int capabilityLevel) {
        this.conditions = conditions;
        this.combinedConditions = LootConditionManager.and(conditions);

        this.count = count;
        this.limit = limit;
        this.requiredCapability = requiredCapability;
        this.capabilityLevel = capabilityLevel;
    }

    @Override
    public ItemStack apply(ItemStack itemStack, LootContext context) {
        int fortuneLevel = 0;

        // todo 1.14: needs validation, used to be killer entity but this looks more correct
        Entity entity = context.get(LootParameters.THIS_ENTITY);

        if (entity != null && requiredCapability != null) {
            ItemStack providingStack = CapabilityHelper.getProvidingItemStack(requiredCapability, capabilityLevel, entity);
            if (!providingStack.isEmpty() && providingStack.getItem() instanceof ItemModular) {
                fortuneLevel = ((ItemModular) providingStack.getItem()).getEffectLevel(providingStack, ItemEffect.fortune);
            }
        }

        itemStack.grow(Math.round(fortuneLevel * count.generateFloat(context.getRandom())));

        if (limit != 0 && itemStack.getCount() > limit) {
            itemStack.setCount(limit);
        }

        return itemStack;
    }

    // todo 1.14: vanilla loot functions seem to do this to conditions, but why?
    public void func_215856_a(ValidationResults p_215856_1_, Function<ResourceLocation, LootTable> p_215856_2_,
            Set<ResourceLocation> p_215856_3_, LootParameterSet p_215856_4_) {
        ILootFunction.super.func_215856_a(p_215856_1_, p_215856_2_, p_215856_3_, p_215856_4_);

        for(int i = 0; i < this.conditions.length; ++i) {
            this.conditions[i].func_215856_a(p_215856_1_.descend(".conditions[" + i + "]"), p_215856_2_, p_215856_3_, p_215856_4_);
        }

    }

    public static class Serializer extends ILootFunction.Serializer<FortuneBonusFunction> {
        public Serializer() {
            super(new ResourceLocation("tetra:fortune_enchant"), FortuneBonusFunction.class);
        }

        public void serialize(JsonObject json, FortuneBonusFunction object, JsonSerializationContext serializationContext) {
            json.add("count", serializationContext.serialize(object.count));
            json.add("limit", serializationContext.serialize(object.limit));
            json.add("requiredCapability", serializationContext.serialize(object.requiredCapability));
            json.add("capabilityLevel", serializationContext.serialize(object.capabilityLevel));

        }

        public FortuneBonusFunction deserialize(JsonObject object, JsonDeserializationContext deserializationContext) {
            ILootCondition[] conditions = JSONUtils.deserializeClass(object, "conditions", new ILootCondition[0], deserializationContext, ILootCondition[].class);
            return new FortuneBonusFunction(
                    conditions,
                    JSONUtils.deserializeClass(object, "count", deserializationContext, RandomValueRange.class),
                    JSONUtils.getInt(object, "limit", 0),
                    Capability.valueOf(JSONUtils.getString(object, "requiredCapability")),
                    JSONUtils.getInt(object, "capabilityLevel", -1));
        }
    }
}