package se.mickelus.tetra.loot;

import com.google.gson.*;
import net.minecraft.util.JSONUtils;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

public class LootPoolDeserializer implements JsonDeserializer<LootPool> {
    @Override
    public LootPool deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = JSONUtils.getJsonObject(json, "loot pool");

        String name = JSONUtils.getString(jsonObject, "name");
        LootEntry[] entries = JSONUtils.deserializeClass(jsonObject, "entries", context, LootEntry[].class);
        entries = Arrays.stream(entries)
                .filter(Objects::nonNull)
                .toArray(LootEntry[]::new);

        LootCondition[] conditions = JSONUtils.deserializeClass(jsonObject, "conditions", new LootCondition[0], context, LootCondition[].class);

        RandomValueRange rolls = JSONUtils.deserializeClass(jsonObject, "rolls", context, RandomValueRange.class);
        RandomValueRange bonusRolls = JSONUtils.deserializeClass(jsonObject, "bonus_rolls", new RandomValueRange(0.0F, 0.0F), context, RandomValueRange.class);

        return new LootPool(entries, conditions, rolls, bonusRolls, name);
    }
}