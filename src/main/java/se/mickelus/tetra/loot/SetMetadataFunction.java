package se.mickelus.tetra.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Allows more precise setting of metadata for items in a loot table. Allows allows altering metadata for items
 * which cannot be damaged.
 */
public class SetMetadataFunction extends LootFunction {
    private final RandomValueRange meta;

    public SetMetadataFunction(LootCondition[] conditionsIn, RandomValueRange damageRangeIn) {
        super(conditionsIn);
        this.meta = damageRangeIn;
    }

    public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
        return new ItemStack(stack.getItem(), stack.getCount(), meta.generateInt(rand));
    }

    public static class Serializer extends LootFunction.Serializer<SetMetadataFunction> {
        public Serializer()
        {
            super(new ResourceLocation("tetra:set_metadata"), SetMetadataFunction.class);
        }

        public void serialize(JsonObject object, SetMetadataFunction functionClazz, JsonSerializationContext serializationContext) {
            object.add("meta", serializationContext.serialize(functionClazz.meta));
        }

        public SetMetadataFunction deserialize(JsonObject object, JsonDeserializationContext deserializationContext, LootCondition[] conditionsIn) {
            return new SetMetadataFunction(conditionsIn, JsonUtils.deserializeClass(object, "meta", deserializationContext, RandomValueRange.class));
        }
    }
}
