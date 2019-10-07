package se.mickelus.tetra.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootFunction;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.ILootCondition;

/**
 * Allows more precise setting of metadata for items in a loot table. Allows altering metadata for items
 * which cannot be damaged.
 * todo 1.14: remove this since it was just used to put lapis in loot tables? (separate item now)
 */
public class SetMetadataFunction extends LootFunction {
    private final RandomValueRange meta;

    public SetMetadataFunction(ILootCondition[] conditionsIn, RandomValueRange damageRangeIn) {
        super(conditionsIn);
        this.meta = damageRangeIn;
    }

    public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
        return new ItemStack(stack.getItem(), stack.getCount());// meta.generateInt(rand));
    }

    @Override
    protected ItemStack doApply(ItemStack stack, LootContext context) {
        return new ItemStack(stack.getItem(), stack.getCount()); // meta.generateInt(context.getRandom()));
    }

    public static class Serializer extends LootFunction.Serializer<SetMetadataFunction> {
        public Serializer()
        {
            super(new ResourceLocation("tetra:set_metadata"), SetMetadataFunction.class);
        }

        public void serialize(JsonObject object, SetMetadataFunction functionClazz, JsonSerializationContext serializationContext) {
            object.add("meta", serializationContext.serialize(functionClazz.meta));
        }

        @Override
        public SetMetadataFunction deserialize(JsonObject object, JsonDeserializationContext deserializationContext, ILootCondition[] conditionsIn) {
            return new SetMetadataFunction(conditionsIn, JSONUtils.deserializeClass(object, "meta", deserializationContext, RandomValueRange.class));
        }
    }
}