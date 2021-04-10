package se.mickelus.tetra.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.scroll.ScrollData;

public class ScrollDataFunction extends LootFunction {
    public static final ResourceLocation identifier = new ResourceLocation(TetraMod.MOD_ID, "scroll");
    public static final LootFunctionType type = new LootFunctionType(new Serializer());

    private ScrollData data;

    protected ScrollDataFunction(ILootCondition[] conditions, ScrollData data) {
        super(conditions);

        this.data = data;
    }

    @Override
    protected ItemStack doApply(ItemStack itemStack, LootContext context) {
        data.write(itemStack);
        return itemStack;
    }

    @Override
    public LootFunctionType getFunctionType() {
        return type;
    }

    public static class Serializer extends LootFunction.Serializer<ScrollDataFunction> {
        public void serialize(JsonObject json, ScrollDataFunction dataFunction, JsonSerializationContext context) {
            super.serialize(json, dataFunction, context);

            dataFunction.data.write(json);
        }

        @Override
        public ScrollDataFunction deserialize(JsonObject json, JsonDeserializationContext context, ILootCondition[] conditions) {
            return new ScrollDataFunction(conditions, ScrollData.read(json));
        }
    }
}
