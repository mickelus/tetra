package se.mickelus.tetra.advancements;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.util.JsonOptional;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlockUseCriterion extends CriterionInstance {
    private final PropertyMatcher before;
    private final PropertyMatcher after;

    private final ItemPredicate item;

    private final Map<String, String> data;

    public static final GenericTrigger<BlockUseCriterion> trigger = new GenericTrigger<>("tetra:block_use", BlockUseCriterion::deserialize);

    public BlockUseCriterion(EntityPredicate.AndPredicate playerCondition, PropertyMatcher before, PropertyMatcher after, ItemPredicate item, Map<String, String> data) {
        super(trigger.getId(), playerCondition);
        this.before = before;
        this.after = after;
        this.item = item;
        this.data = data;
    }

    public static void trigger(ServerPlayerEntity player, BlockState state, ItemStack usedItem, Map<String, String> data) {
        trigger.fulfillCriterion(player, criterion -> criterion.test(state, usedItem, data));
    }

    public static void trigger(ServerPlayerEntity player, BlockState state, ItemStack usedItem) {
        trigger(player, state, usedItem, Collections.emptyMap());
    }

    public boolean test(BlockState state, ItemStack usedItem, Map<String, String> data) {
        if (before != null && !before.test(state)) {
            return false;
        }

        if (after != null && !after.test(state)) {
            return false;
        }

        if (item != null && !item.test(usedItem)) {
            return false;
        }

        if (this.data != null) {
            boolean hasUnmatched = this.data.entrySet().stream()
                    .anyMatch(entry -> !data.containsKey(entry.getKey()) || !entry.getValue().equals(data.get(entry.getKey())));
            if (hasUnmatched) {
                return false;
            }
        }

        return true;
    }

    private static BlockUseCriterion deserialize(JsonObject json, EntityPredicate.AndPredicate entityPredicate, ConditionArrayParser conditionsParser) {
        return new BlockUseCriterion(entityPredicate,
                JsonOptional.field(json, "before")
                        .map(PropertyMatcher::deserialize)
                        .orElse(null),
                JsonOptional.field(json, "after")
                        .map(PropertyMatcher::deserialize)
                        .orElse(null),
                JsonOptional.field(json, "item")
                        .map(ItemPredicate::deserialize)
                        .orElse(null),
                JsonOptional.field(json, "data")
                        .map(JsonElement::getAsJsonObject)
                        .map(JsonObject::entrySet)
                        .map(Collection::stream)
                        .orElseGet(Stream::empty)
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getAsString())));
    }
}
