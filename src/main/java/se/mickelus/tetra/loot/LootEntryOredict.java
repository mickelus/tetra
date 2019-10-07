package se.mickelus.tetra.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.SetNBT;
import net.minecraftforge.oredict.OreDictionary;
import se.mickelus.tetra.items.forged.ItemMetalScrap;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class LootEntryOredict extends LootEntryItem {
    public LootEntryOredict(Item item, int weight, int quality, LootFunction[] functions, LootCondition[] conditions, String entryName) {
        super(item, weight, quality, functions, conditions, entryName);
    }

    @Nullable
    public static LootEntryItem deserialize(JsonObject jsonObject, JsonDeserializationContext deserializationContext, int weight, int quality, LootCondition[] conditions) {
        String oreName = JSONUtils.getString(jsonObject, "ore");
        return OreDictionary.getOres(oreName).stream()
                .filter(itemStack -> !itemStack.isEmpty())
                .min(Comparator.comparing(itemStack -> itemStack.getItem().getUnlocalizedName()))
                .map(itemStack -> {
                    List<LootFunction> functions = new ArrayList<>();

                    if (itemStack.getMetadata() != 0) {
                        functions.add(new SetMetadataFunction(conditions, new RandomValueRange(itemStack.getMetadata())));
                    }

                    if (itemStack.hasTagCompound()) {
                        functions.add(new SetNBT(conditions, itemStack.getTagCompound()));
                    }

                    functions.addAll(Arrays.asList(
                            JSONUtils.deserializeClass(jsonObject, "functions", new LootFunction[0], deserializationContext, LootFunction[].class)));

                    return new LootEntryOredict(itemStack.getItem(), weight, quality, functions.toArray(new LootFunction[0]), conditions, oreName);
                })
                .orElse(null);
    }
}
