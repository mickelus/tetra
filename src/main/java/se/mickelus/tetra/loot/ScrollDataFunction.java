package se.mickelus.tetra.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.scroll.ScrollData;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class ScrollDataFunction extends LootItemConditionalFunction {
    public static final ResourceLocation identifier = new ResourceLocation(TetraMod.MOD_ID, "scroll");
    public static final LootItemFunctionType type = new LootItemFunctionType(new Serializer());

    private ScrollData data;

    protected ScrollDataFunction(LootItemCondition[] conditions, ScrollData data) {
        super(conditions);

        this.data = data;
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext context) {
        data.write(itemStack);
        return itemStack;
    }

    @Override
    public LootItemFunctionType getType() {
        return type;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<ScrollDataFunction> {
        public void serialize(JsonObject json, ScrollDataFunction dataFunction, JsonSerializationContext context) {
            super.serialize(json, dataFunction, context);

            dataFunction.data.write(json);
        }

        @Override
        public ScrollDataFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new ScrollDataFunction(conditions, ScrollData.read(json));
        }
    }
}
