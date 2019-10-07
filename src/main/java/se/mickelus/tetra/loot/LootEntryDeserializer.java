package se.mickelus.tetra.loot;

import com.google.gson.*;
import net.minecraft.item.Item;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryEmpty;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootEntryTable;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.common.ForgeHooks;

import java.lang.reflect.Type;

public class LootEntryDeserializer implements JsonDeserializer<LootEntry> {
    public LootEntry deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = JSONUtils.getJsonObject(json, "loot item");

        String entryType = JSONUtils.getString(jsonObject, "type");
        int weight = JSONUtils.getInt(jsonObject, "weight", 1);
        int quality = JSONUtils.getInt(jsonObject, "quality", 0);

        LootCondition[] conditions = JSONUtils.deserializeClass(jsonObject, "conditions", new LootCondition[0], context, LootCondition[].class);

        LootEntry ret = net.minecraftforge.common.ForgeHooks.deserializeJsonLootEntry(entryType, jsonObject, weight, quality, conditions);
        if (ret != null) return ret;

        try {
            if ("item".equals(entryType)) {
                return deserializeItem(jsonObject, context, weight, quality, conditions);
            } else if ("loot_table".equals(entryType)) {
                return deserializeTable(jsonObject, context, weight, quality, conditions);
            } else if ("empty".equals(entryType)) {
                return deserializeEmpty(jsonObject, context, weight, quality, conditions);
            } else if ("oredict".equals(entryType)) {
                return LootEntryOredict.deserialize(jsonObject, context, weight, quality, conditions);
            }
        } catch (JsonParseException e) {
            e.printStackTrace();
            // todo: log info level about missing type or faulty entry
        }

        return null;
    }

    private static String getEntryName(JsonObject json, String fallback) {
        if (json.has("entryName")) {
            return JSONUtils.getString(json, "entryName");
        }

        return fallback;
    }

    private LootEntryItem deserializeItem(JsonObject jsonObject, JsonDeserializationContext context, int weight, int quality, LootCondition[] conditions) {
        String name = getEntryName(jsonObject, JSONUtils.getString(jsonObject, "name"));

        try {
            Item item = JSONUtils.getItem(jsonObject, "name");
            LootFunction[] functions = JSONUtils.deserializeClass(jsonObject, "functions", new LootFunction[0], context, LootFunction[].class);
            return new LootEntryItem(item, weight, quality, functions, conditions, name);
        } catch (JsonSyntaxException e) {
            // we expect this to throw for some modded items
            // todo: add debug log
        }

        return null;
    }

    private LootEntryTable deserializeTable(JsonObject jsonObject, JsonDeserializationContext context, int weight, int quality, LootCondition[] conditions) {
        String name = getEntryName(jsonObject, JSONUtils.getString(jsonObject, "name"));
        ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getString(jsonObject, "name"));
        return new LootEntryTable(resourcelocation, weight, quality, conditions, name);
    }

    public static LootEntryEmpty deserializeEmpty(JsonObject jsonObject, JsonDeserializationContext context, int weight, int quality, LootCondition[] conditions) {
        return new LootEntryEmpty(weight, quality, conditions, getEntryName(jsonObject, "empty"));
    }
}