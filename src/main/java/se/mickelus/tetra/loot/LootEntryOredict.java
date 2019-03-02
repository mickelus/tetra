package se.mickelus.tetra.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.SetNBT;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LootEntryOredict extends LootEntryItem {
    public LootEntryOredict(Item item, int weight, int quality, LootFunction[] functions, LootCondition[] conditions, String entryName) {
        super(item, weight, quality, functions, conditions, entryName);
    }

    public static LootEntryItem deserialize(JsonObject jsonObject, JsonDeserializationContext deserializationContext, int weight, int quality, LootCondition[] conditions) {
        String oreName = JsonUtils.getString(jsonObject, "ore");
        NonNullList<ItemStack> itemStacks = OreDictionary.getOres(oreName);

        if (!itemStacks.isEmpty()) {
            ItemStack itemStack = itemStacks.get(0);

            List<LootFunction> functions = new ArrayList<>();

            if (itemStack.getMetadata() != 0) {
                functions.add(new SetMetadataFunction(conditions, new RandomValueRange(itemStack.getMetadata())));
            }

            if (itemStack.hasTagCompound()) {
                functions.add(new SetNBT(conditions, itemStack.getTagCompound()));
            }

            functions.addAll(Arrays.asList(
                    JsonUtils.deserializeClass(jsonObject, "functions", new LootFunction[0], deserializationContext, LootFunction[].class)));

            return new LootEntryOredict(itemStack.getItem(), weight, quality, functions.toArray(new LootFunction[0]), conditions, oreName);
        }

        return null;
    }
}
